(ns parts.test-helpers)

(defn chan?
  [x]
  #?(:clj  (instance? clojure.core.async.impl.channels.ManyToManyChannel x)
     :cljs (instance?    cljs.core.async.impl.channels.ManyToManyChannel x)))

