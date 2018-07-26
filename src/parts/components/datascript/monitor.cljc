(ns parts.components.datascript.monitor
  (:require
   [com.stuartsierra.component :as c]
   [datascript.core :as dts]
   [datascript-schema.core :as dtssch]
   #?@(:clj  [[clojure.spec.alpha :as s]]
       :cljs [[cljs.spec.alpha :as s]])))

;; ---- datascript monitor spec ----

(s/def ::conn
  dts/conn?)

(s/def ::datascript
  (s/keys :req-un [::conn]))

(s/def ::callback
  fn?)

(s/def ::datascript-monitor-params
  (s/keys :req-un [::datascript ::callback]))

;; ---- datascript monitor ----

(defrecord DatascriptMonitor [datascript callback started?]
  c/Lifecycle
  (start [{:keys [datascript callback started?] :as this}]
    (s/assert ::datascript-monitor-params this)
    (if started?
      this
      (do (dtssch/listen-on-schema-change! (:conn datascript) callback)
          (assoc this :started? true))))
  (stop [{:keys [datascript started?] :as this}]
    (if-not started?
      this
      (do (dtssch/unlisten-schema-change! (:conn datascript))
          (assoc this :started? false)))))

(defn make-datascript-monitor
  [{:keys [callback]}]
  (map->DatascriptMonitor {:callback callback
                           :started? false}))
