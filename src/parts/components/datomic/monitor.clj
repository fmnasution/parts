(ns parts.components.datomic.monitor
  (:require
   [clojure.spec.alpha :as s]
   [com.stuartsierra.component :as c]
   [datomic.api :as dtm]))

;; ---- datomic monitor spec ----

(s/def ::conn
  #(instance? datomic.Connection %))

(s/def ::datomic
  (s/keys :req-un [::conn]))

(s/def ::callback
  fn?)

(s/def ::datomic-monitor-params
  (s/keys :req-un [::datomic ::callback]))

;; ---- datomic monitor ----

(defrecord DatomicMonitor [datomic callback active?_]
  c/Lifecycle
  (start [{:keys [datomic callback active?_] :as this}]
    (s/assert ::datomic-monitor-params this)
    (if (some? active?_)
      this
      (let [active?_        (atom true)
            tx-report-queue (dtm/tx-report-queue (:conn datomic))]
        (future
          (while @active?_
            (let [tx-report (.take tx-report-queue)]
              (callback tx-report))))
        (assoc this :active?_ active?_))))
  (stop [{:keys [active?_] :as this}]
    (if (nil? active?_)
      this
      (do (reset! active?_ false)
          (assoc this :active?_ nil)))))

(defn make-datomic-monitor
  [{:keys [callback]}]
  (map->DatomicMonitor {:callback callback}))
