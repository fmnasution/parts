(ns parts.components.async
  (:require
   [com.stuartsierra.component :as c]
   #?@(:clj  [[clojure.core.async :as a :refer [go-loop]]]
       :cljs [[cljs.core.async :as a]]))
  #?@(:cljs
      (:require-macros
       [cljs.core.async.macros :refer [go-loop]])))

;; ---- item dispatcher ----

(defn- chan
  [v]
  (if (pos-int? v)
    (a/chan v)
    (let [{:keys [fixed sliding dropping]} v]
      (cond
        (pos-int? fixed)
        (a/chan (a/buffer fixed))

        (pos-int? sliding)
        (a/chan (a/sliding-buffer sliding))

        (pos-int? dropping)
        (a/chan (a/dropping-buffer dropping))

        :else (a/chan)))))

(defrecord ItemDispatcher [config item-chan]
  c/Lifecycle
  (start [{:keys [config item-chan] :as this}]
    (if (some? item-chan)
      this
      (let [item-chan (chan (:chan config))]
        (assoc this :item-chan item-chan))))
  (stop [{:keys [item-chan] :as this}]
    (if (nil? item-chan)
      this
      (do (a/close! item-chan)
          (assoc this :item-chan nil)))))

(defn make-item-dispatcher
  [{:keys [config]}]
  (map->ItemDispatcher {:config config}))

;; ---- channel pipeliner ----

(defrecord ChannelPipeliner [kind
                             parallelism
                             to-key
                             to
                             xform-fn
                             from-key
                             from
                             close-both?
                             ex-handler
                             started?]
  c/Lifecycle
  (start [{:keys [kind
                  parallelism
                  to-key
                  to
                  xform-fn
                  from-key
                  from
                  close-both?
                  ex-handler
                  started?]
           :as   this}]
    (if started?
      this
      (let [pipeline!   (case kind
                          :normal   a/pipeline
                          :async    a/pipeline-async
                          :blocking a/pipeline-blocking
                          a/pipeline)
            parallelism (or parallelism 1)
            to-chan     (get to to-key)
            xform       (xform-fn this)
            from-chan   (get from from-key)
            close-both? (if (nil? close-both?) true close-both?)]
        (pipeline! parallelism to-chan xform from-chan close-both? ex-handler)
        (assoc this :started? true))))
  (stop [{:keys [started?] :as this}]
    (cond-> this
      started?
      (assoc :started? false))))

(defn make-channel-pipeliner
  [{:keys [kind parallelism to-key xform-fn from-key close-both? ex-handler]}]
  (map->ChannelPipeliner {:kind        kind
                          :parallelism parallelism
                          :to-key      to-key
                          :xform-fn    xform-fn
                          :from-key    from-key
                          :close-both? close-both?
                          :ex-handler  ex-handler
                          :started?    false}))

;; ---- channel listener ----

(defrecord ChannelListener [channel-paths callback stop-chan]
  c/Lifecycle
  (start [{:keys [channel-paths callback stop-chan] :as this}]
    (if (some? stop-chan)
      this
      (let [stop-chan (a/chan)
            chans     (into [stop-chan] (keep #(get-in this %)) channel-paths)]
        (go-loop []
          (let [[item chan] (a/alts! chans :priority true)
                stop?       (or (= stop-chan chan) (nil? item))]
            (when-not stop?
              (callback this item)
              (recur))))
        (assoc this :stop-chan stop-chan))))
  (stop [{:keys [stop-chan] :as this}]
    (if (nil? stop-chan)
      this
      (do (a/close! stop-chan)
          (assoc this :stop-chan nil)))))

(defn make-channel-listener
  [{:keys [channel-paths callback]}]
  (map->ChannelListener {:channel-paths channel-paths
                         :callback      callback}))
