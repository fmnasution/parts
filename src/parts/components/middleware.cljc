(ns parts.components.middleware
  (:require
   [com.stuartsierra.component :as c]
   #?@(:clj  [[clojure.spec.alpha :as s]]
       :cljs [[cljs.spec.alpha :as s]])))

;; ---- midleware spec ----

(s/def ::entries
  (s/cat :entries
         (s/+ (s/or :entry     fn?
                    :with-args (s/cat :entry fn?
                                      :args  (s/* any?))))))

(s/def ::middleware-params
  (s/keys :req-un [::entries]))

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

(defrecord Middleware [entries wrapper]
  c/Lifecycle
  (start [{:keys [entries wrapper] :as this}]
    (s/assert ::middleware-params this)
    (cond-> this
      (nil? wrapper)
      (assoc :wrapper (compose-middleware this entries))) )
  (stop [{:keys [wrapper] :as this}]
    (cond-> this
      (some? wrapper)
      (assoc :wrapper nil))))

(defn make-middleware
  [{:keys [entries]}]
  (map->Middleware {:entries entries}))
