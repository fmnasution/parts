(ns parts.components.bidi
  (:require
   [clojure.spec.alpha :as s]
   [com.stuartsierra.component :as c]
   [ring.util.http-response :as resp]
   [bidi.ring :refer [make-handler]]
   [parts.components.util :as cu]))

;; ---- ring router spec ----

(s/def ::routes
  ::cu/routes)

(s/def ::resources
  ::cu/resources)

(s/def ::not-found-handler
  (s/nilable fn?))

(s/def ::ring-router-params
  (s/keys :req-un [::routes ::resources ::not-found-handler]))

;; ---- ring router ----

(defrecord RingRouter [routes resources not-found-handler handler]
  c/Lifecycle
  (start [{:keys [routes resources not-found-handler handler]
           :as   this}]
    (s/assert ::ring-router-params this)
    (if (some? handler)
      this
      (let [not-found-handler (or not-found-handler
                                  (constantly (resp/not-found)))
            base-handler      (make-handler routes resources)
            handler           #(or (base-handler %)
                                   (not-found-handler %))]
        (assoc this :handler handler))))
  (stop [{:keys [handler] :as this}]
    (cond-> this
      (some? handler)
      (assoc :handler nil))))

(defn make-ring-router
  [{:keys [routes resources not-found-handler]}]
  (map->RingRouter {:routes            routes
                    :resources         resources
                    :not-found-handler not-found-handler}))
