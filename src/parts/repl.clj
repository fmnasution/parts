(ns parts.repl
  (:require
   [boot.util :as bu]
   [com.stuartsierra.component :as c]
   [clojure.tools.namespace.repl :refer [refresh]]
   [clojure.tools.namespace.reload :as nsreload]))

(def system
  nil)

(def initializer
  nil)

(defn setup!
  [sym]
  (alter-var-root #'initializer (constantly sym)))

(defn boot!
  []
  (alter-var-root #'system (constantly ((find-var initializer))))
  (alter-var-root #'system c/start)
  :ok)

(defn shutdown!
  []
  (alter-var-root #'system
                  (fn [sys]
                    (when (some? sys)
                      (c/stop sys))))
  :ok)

(defn reboot!
  [tracker restart?]
  (when restart?
    (bu/info "Shutting down system...\n")
    (shutdown!))
  (bu/info "Reloading namespaces...\n")
  (nsreload/track-reload tracker)
  (when restart?
    (bu/info "Starting back system...\n")
    (boot!)))
