(ns parts.components.bidi
  (:require
   [com.stuartsierra.component :as c]
   [bidi.router :refer [start-router!]]))

;; ---- html router ----

(defrecord HtmlRouter [routes on-navigate default-location router]
  c/Lifecycle
  (start [{:keys [routes on-navigate default-location router] :as this}]
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
