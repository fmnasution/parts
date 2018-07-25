(ns parts.components.http-kit
  (:require
   [clojure.spec.alpha :as s]
   [com.stuartsierra.component :as c]
   [ring.util.http-response :as resp]
   [org.httpkit.server :refer [run-server]]))

;; ---- web server spec ----

(s/def ::config
  map?)

(s/def ::handler
  fn?)

(s/def ::ring-handler
  (s/nilable (s/keys :req-un [::handler])))

(s/def ::wrapper
  fn?)

(s/def ::ring-middleware
  (s/nilable (s/keys :req-un [::wrapper])))

(s/def ::web-server-params
  (s/keys :req-un [::config ::ring-handler ::ring-middleware]))

;; ---- web server ----

(defrecord WebServer [config ring-handler ring-middleware server]
  c/Lifecycle
  (start [{:keys [config server ring-handler ring-middleware] :as this}]
    (s/assert ::web-server-params this)
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
