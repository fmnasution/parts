(ns parts.repl
  (:require
   [com.stuartsierra.component :as c]
   [clojure.tools.namespace.repl :refer [refresh]]))

(def system
  nil)

(defn setup!
  [system-fn]
  (alter-var-root #'system system-fn))

(defn boot!
  []
  (alter-var-root #'system c/start)
  :ok)

(defn shutdown!
  []
  (alter-var-root #'system
                  (fn [sys]
                    (when sys
                      (try (c/stop sys)
                           (catch Throwable t
                             (println t)
                             sys)))))
  :ok)

(defn reboot!
  []
  (shutdown!)
  (let [ret (refresh :after `boot!)]
    (if (instance? Throwable ret)
      (throw ret)
      ret)))
