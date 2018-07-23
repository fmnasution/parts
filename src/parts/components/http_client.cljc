(ns parts.components.http-client
  (:require
   [com.stuartsierra.component :as c]
   [bidi.bidi :as b]
   [ajax.core :as jx]))

;; ---- http client ----

(defn- request!
  [routes handler route-params request-method option]
  (let [uri        (apply b/path-for routes handler (flatten (seq route-params)))
        request-fn (case request-method
                     :get    jx/GET
                     :post   jx/POST
                     :put    jx/PUT
                     :delete jx/DELETE)]
    (request-fn uri option)))

(defrecord HttpClient [routes option-middleware caller]
  c/Lifecycle
  (start [{:keys [routes option-middleware caller] :as this}]
    (if (some? caller)
      this
      (let [caller (fn [routes handler route-params request-method option]
                     (let [wrapper (:wrapper option-middleware identity)]
                       (request! routes
                                 handler
                                 route-params
                                 request-method
                                 ((wrapper identity) option))))]
        (assoc this :caller caller))))
  (stop [{:keys [caller] :as this}]
    (cond-> this
      (some? caller)
      (assoc :caller nil))))

(defn make-http-client
  [{:keys [routes]}]
  (map->HttpClient {:routes routes}))
