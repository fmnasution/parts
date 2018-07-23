(ns parts.components.http-kit
  (:require
   [com.stuartsierra.component :as c]
   [ring.util.http-response :as resp]
   [org.httpkit.server :refer [run-server]]))

;; ---- web server ----

(defrecord WebServer [config server ring-handler ring-middleware]
  c/Lifecycle
  (start [{:keys [config server ring-handler ring-middleware] :as this}]
    (if (some? server)
      this
      (let [middleware (:wrapper ring-middleware identity)
            handler    (:handler ring-handler
                                 (constantly (resp/service-unavailable)))
            server     (run-server (middleware handler) config)]
        (assoc this :server server))))
  (stop [{:keys [server] :as this}]
    (if (nil? server)
      this
      (do (server :timeout 100)
          (assoc this :server nil)))))

(defn make-web-server
  [{:keys [config ring-handler ring-middleware]}]
  (map->WebServer {:config          config
                   :ring-handler    ring-handler
                   :ring-middleware ring-middleware}))
