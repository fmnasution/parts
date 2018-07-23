(ns parts.components.middleware
  (:require
   [com.stuartsierra.component :as c]))

;; ---- middleware ----

(defn- replace-with-component
  [component entry]
  (if (vector? entry)
    (replace {:component component} entry)
    entry))

(defn- as-middleware
  [entry]
  (if (vector? entry)
    #(apply (first entry) % (rest entry))
    entry))

(defn- compose-middleware
  [component entries]
  (apply comp (into []
                    (comp
                     (map #(replace-with-component component %))
                     (map as-middleware))
                    entries)))

(defn- create-middleware
  [{:keys [entries wrapper] :as this}]
  (cond-> this
    (nil? wrapper)
    (assoc :wrapper (compose-middleware this entries))))

(defn- destroy-middleware
  [{:keys [wrapper] :as this}]
  (cond-> this
    (some? wrapper)
    (assoc :wrapper nil)))

(defrecord Middleware [entries wrapper]
  c/Lifecycle
  (start [this]
    (create-middleware this))
  (stop [this]
    (destroy-middleware this)))

(defn make-middleware
  [{:keys [entries]}]
  (map->Middleware {:entries entries}))
