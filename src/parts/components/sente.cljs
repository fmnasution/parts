(ns parts.components.sente
  (:require
   [cljs.spec.alpha :as s]
   [cljs.core.async :as a]
   [com.stuartsierra.component :as c]
   [taoensso.sente :as sente :refer [make-channel-socket!]]
   [taoensso.sente.interfaces :refer [IPacker]]
   [parts.components.util :as cu]))

;; ---- websocket client params ----

(s/def ::server-uri
  cu/nblank-str?)

(s/def ::type
  #{:auto :ws :ajax})

(s/def ::protocol
  #{:http :https})

(s/def ::host
  pos-int?)

(s/def ::params
  map?)

(s/def ::packer
  (s/or :edn    #(= :edn %)
        :packer #(satisfies? IPacker %)))

(s/def ::ajax-opts
  map?)

(s/def ::wrap-recv-evs?
  boolean?)

(s/def ::ws-kalive-ms
  pos-int?)

(s/def ::client-option
  (s/nilable (s/keys :opt-un [::type
                              ::protocol
                              ::host
                              ::params
                              ::packer
                              ::ajax-opts
                              ::wrap-recv-evs?
                              ::ws-kalive-ms])))

(s/def ::websocket-client-params
  (s/keys :req-un [::server-uri ::client-option]))

;; ---- websocket client ----

(defrecord WebsocketClient [server-uri
                            client-option
                            chsk
                            recv-chan
                            send!
                            state]
  c/Lifecycle
  (start [{:keys [recv-chan] :as this}]
    (s/assert ::websocket-client-params this)
    (if (some? recv-chan)
      this
      (let [{:keys [chsk ch-recv send-fn state]}
            (make-channel-socket! server-uri client-option)]
        (assoc this
               :chsk      chsk
               :recv-chan ch-recv
               :send!     send-fn
               :state     state))))
  (stop [{:keys [chsk recv-chan] :as this}]
    (if (nil? recv-chan)
      this
      (do (sente/chsk-disconnect! chsk)
          (a/close! recv-chan)
          (assoc this
                 :chsk      nil
                 :recv-chan nil
                 :send!     nil
                 :state     nil)))))

(defn make-websocket-client
  [{:keys [server-uri client-option]}]
  (map->WebsocketClient {:server-uri    server-uri
                         :client-option client-option}))
