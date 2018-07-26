(ns parts.components.datasript
  (:require
   [com.stuartsierra.component :as c]
   [datascript.core :as dts]
   #?@(:clj  [[clojure.spec.alpha :as s]]
       :cljs [[cljs.spec.alpha :as s]])))

;; ---- datascript spec ----

(s/def ::schema
  (s/map-of keyword? (s/map-of keyword? keyword?)))

(s/def ::config
  (s/nilable (s/keys :opt-un [::schema])))

(s/def ::datascript-params
  (s/keys :req-un [::config]))

;; ---- datascript ----

(defrecord Datascript [config conn]
  c/Lifecycle
  (start [{:keys [conn] :as this}]
    (s/assert ::datascript-params this)
    (if (some? conn)
      this
      (let [schema (:schema config {})
            conn   (dts/create-conn schema)]
        (assoc this :conn conn))))
  (stop [{:keys [conn] :as this}]
    (cond-> this
      (some? conn)
      (assoc :conn nil))))

(defn make-datascript
  [{:keys [config]}]
  (map->Datascript {:config config}))
