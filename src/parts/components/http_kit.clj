(ns parts.components.http-kit
  (:require
   [clojure.spec.alpha :as s]
   [com.stuartsierra.component :as c]
   [ring.util.http-response :as resp]
   [org.httpkit.server :refer [run-server]]
   [parts.components.util :as cu]))

;; ---- web server spec ----

(s/def ::ip
  ::cu/nblank-str)

(s/def ::port
  pos-int?)

(s/def ::thread
  pos-int?)

(s/def ::queue-size
  pos-int?)

(s/def ::max-body
  pos-int?)

(s/def ::max-ws
  pos-int?)

(s/def ::max-line
  pos-int?)

(s/def ::proxy-protocol
  #{:disable :enable :optional})

(s/def ::worker-name-prefix
  ::cu/nblank-str)

(s/def ::worker-pool
  #(instance? java.util.concurrent.ExecutorService %))

(s/def ::error-logger
  fn?)

(s/def ::warn-logger
  fn?)

(s/def ::event-logger
  fn?)

(s/def ::event-names
  map?)

(s/def ::config
  (s/keys :opt-un [::ip
                   ::port
                   ::thread
                   ::queue-size
                   ::max-body
                   ::max-ws
                   ::max-line
                   ::proxy-protocol
                   ::worker-name-prefix
                   ::worker-pool
                   ::error-logger
                   ::warn-logger
                   ::event-logger
                   ::event-names]))

(s/def ::handler
  fn?)

(s/def ::ring-handler
  (s/keys :req-un [::handler]))

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
            handler    (:handler ring-handler)
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
