(ns parts.components.datomic-test
  (:require
   [clojure.test :refer [deftest is testing are]]
   [clojure.java.io :as jio]
   [com.stuartsierra.component :as c]
   [datomic.api :as dtm]
   [io.rkn.conformity :as dtmcnf]
   [parts.components.datomic :as pcdtm]))

(let [uri "datomic:mem://parts-test"]
  (deftest datomic-component
    (testing "datomic lifecycle"
      (let [started (c/start (pcdtm/make-datomic {:config {:uri uri}}))
            stopped (c/stop started)]
        (is (instance? datomic.Connection (:conn started)))
        (is (nil? (:conn stopped)))))
    (testing "conforming datomic"
      (testing "target schema exists"
        (let [path    "private/parts/datomic_schema_test.edn"
              started (c/start (pcdtm/make-datomic {:config {:uri  uri
                                                             :path path}}))]
          (is (dtmcnf/has-attribute? (dtm/db (:conn started)) :test/attribute))
          (c/stop started)))
      (testing "target schema doesn't exists"
        (let [path    "/"
              started (c/start (pcdtm/make-datomic {:config {:uri  uri
                                                             :path path}}))]
          (is (not (dtmcnf/has-attribute? (dtm/db (:conn started))
                                          :test/attribute)))
          (c/stop started))))))
