(ns parts.test-helpers
  (:require
   #?@(:clj  [[clojure.core.async :as a]]
       :cljs [[cljs.core.async :as a]])))

(defn chan?
  [x]
  #?(:clj  (instance? clojure.core.async.impl.channels.ManyToManyChannel x)
     :cljs (instance?    cljs.core.async.impl.channels.ManyToManyChannel x)))

