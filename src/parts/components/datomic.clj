(ns parts.components.datomic
  (:require
   [clojure.spec.alpha :as s]
   [com.stuartsierra.component :as c]
   [datomic.api :as dtm]
   [parts.components.util :as cu]))

;; ---- datomic spec ----

(s/def ::uri
  cu/nblank-str?)

(s/def ::config
  (s/keys :req-un [::uri]))

(s/def ::datomic-params
  (s/keys :req-un [::config]))

;; ---- datomic ----

(defrecord Datomic [config conn]
  c/Lifecycle
  (start [{:keys [config conn] :as this}]
    (s/assert ::datomic-params this)
    (if (some? conn)
      this
      (let [uri      (:uri config)
            created? (dtm/create-database uri)
            conn     (dtm/connect uri)]
        (assoc this :conn conn))))
  (stop [{:keys [conn] :as this}]
    (if (nil? conn)
      this
      (do (dtm/release conn)
          (assoc this :conn nil)))))

(defn make-datomic
  [{:keys [config]}]
  (map->Datomic {:config config}))
