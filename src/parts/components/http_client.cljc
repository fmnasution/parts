(ns parts.components.http-client
  (:require
   [com.stuartsierra.component :as c]
   [bidi.bidi :as b]
   [ajax.core :as jx]
   [parts.components.util :as cu]
   #?@(:clj  [[clojure.spec.alpha :as s]]
       :cljs [[cljs.spec.alpha :as s]])))

;; ---- http client spec ----

(s/def ::routes
  ::cu/routes)

(s/def ::wrapper
  fn?)

(s/def ::option-middleware
  (s/nilable (s/keys :req-un [::wrapper])))

(s/def ::http-client-params
  (s/keys :req-un [::routes ::option-middleware]))

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
    (s/assert ::http-client-params this)
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
