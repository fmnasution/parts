(ns parts.components.async-test
  (:require
   [com.stuartsierra.component :as c]
   [parts.components.async :as pcasnc]
   #?@(:clj  [[clojure.test :refer [deftest testing is]]
              [clojure.core.async :as a]]
       :cljs [[cljs.test :refer [deftest testing is]]
              [cljs.core.async :as a]])))

(defn- chan?
  [x]
  #?(:clj  (instance? clojure.core.async.impl.channels.ManyToManyChannel x)
     :cljs (instance?    cljs.core.async.impl.channels.ManyToManyChannel x)))

(deftest item-dispatcher-component
  (testing "item dispatcher lifecycle"
    (let [started (c/start (pcasnc/make-item-dispatcher {:config {}}))
          stopped (c/stop started)]
      (is (chan? (:item-chan started)))
      (is (nil? (:item-chan stopped))))))

(deftest channel-pipeliner-component
  (testing "channel pipeliner lifecycle"
    (let [params  {:kind        :normal
                   :parallelism 1
                   :to-key      :to-key
                   :xform-fn    (constantly (map inc))
                   :from-key    :from-key
                   :close-both? false
                   :ex-handler  identity}
          started (-> (pcasnc/make-channel-pipeliner params)
                      (assoc :to   {:to-key (a/chan)}
                             :from {:from-key (a/chan)})
                      (c/start))
          stopped (c/stop started)]
      (is (:started? started))
      (is (not (:started? stopped)))))
  (testing "pipelining a message"
    (let [new-channel-pipeliner (fn [to-chan xform-fn from-chan]
                                  (-> {:kind        :blocking
                                       :parallelism 1
                                       :to-key      :to-key
                                       :xform-fn    xform-fn
                                       :from-key    :from-key
                                       :close-both? false
                                       :ex-handler  identity}
                                      (pcasnc/make-channel-pipeliner)
                                      (assoc :to   {:to-key to-chan}
                                             :from {:from-key from-chan})))]
      (testing "success"
        (let [to-chan   (a/chan)
              from-chan (a/chan)
              started   (c/start (new-channel-pipeliner
                                  to-chan
                                  (constantly (map inc))
                                  from-chan))]
          (a/>!! from-chan 1)
          (is (= 2 (a/<!! to-chan)))
          (c/stop started)))
      (testing "fail"
        (let [to-chan   (a/chan)
              from-chan (a/chan)
              started   (c/start (new-channel-pipeliner
                                  to-chan
                                  (constantly (map #(/ % 0)))
                                  from-chan))]
          (a/>!! from-chan 1)
          (is (instance? java.lang.ArithmeticException (a/<!! to-chan)))
          (c/stop started))))))

(deftest channel-listener-component
  (testing "channel listener lifecycle"
    (let [started (-> {:channel-paths [[:a :a-key]]
                       :callback      identity}
                      (pcasnc/make-channel-listener)
                      (assoc :a {:a-key (a/chan)})
                      (c/start))
          stopped (c/stop started)]
      (is (chan? (:stop-chan started)))
      (is (nil? (:stop-chan stopped)))))
  (testing "listening a message"
    (let [chan    (a/chan)
          a-chan  (a/chan)
          b-chan  (a/chan)
          started (-> {:channel-paths [[:a :a-key]
                                       [:b :b-key]]
                       :callback      #(a/put! chan %2)}
                      (pcasnc/make-channel-listener)
                      (assoc :a {:a-key a-chan}
                             :b {:b-key b-chan})
                      (c/start))]
      (a/>!! a-chan 1)
      (a/>!! b-chan 2)
      (is (= 1 (a/<!! chan)))
      (is (= 2 (a/<!! chan)))
      (c/stop started))))
