(ns parts.components.bidi
  (:require
   [cljs.spec.alpha :as s]
   [com.stuartsierra.component :as c]
   [bidi.router :refer [start-router!]]
   [parts.components.util :as cu]))

;; ---- html router spec ----

(s/def ::routes
  ::cu/routes)

(s/def ::on-navigate
  fn?)

(s/def ::handler
  keyword?)

(s/def ::route-params
  map?)

(s/def ::default-location
  (s/keys :req-un [::handler]
          :opt-un [::route-params]))

(s/def ::html-router-params
  (s/keys :req-un [::routes ::on-navigate ::default-location]))

;; ---- html router ----

(defrecord HtmlRouter [routes on-navigate default-location router]
  c/Lifecycle
  (start [{:keys [routes on-navigate default-location router] :as this}]
    (s/assert ::html-router-params this)
    (if (some? router)
      this
      (let [router (start-router! routes {:on-navigate      on-navigate
                                          :default-location default-location})]
        (assoc this :router router))))
  (stop [{:keys [router] :as this}]
    (cond-> this
      (some? router)
      (assoc :router nil))))

(defn make-html-router
  [{:keys [routes on-navigate default-location]}]
  (map->HtmlRouter {:routes           routes
                    :on-navigate      on-navigate
                    :default-location default-location}))
