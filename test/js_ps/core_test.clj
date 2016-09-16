(ns js-ps.core-test
  (:require [clojure.test :refer :all]
            [js-ps.core :refer :all]
            [schema.core :as s]))

(deftest simple-types
  (is (= {(s/optional-key :a) s/Int
          (s/optional-key :b) s/Num
          (s/optional-key :c) s/Str
          (s/optional-key :d) s/Bool}
         (->prismatic {:type "object"
                       :properties {:a {:type "integer"}
                                    :b {:type "number"}
                                    :c {:type "string"}
                                    :d {:type "boolean"}}
                       :additionalProperties false}))))

(deftest complex-objects
  (is (= {(s/optional-key :a) {(s/optional-key :c)  s/Int}
          (s/optional-key :b) [{(s/optional-key :d) s/Bool}]}
         (->prismatic {:type "object"
                       :properties {:a {:type "object"
                                        :properties {:c {:type "integer"}}
                                        :additionalProperties false}
                                    :b {:type "array"
                                        :items {:type "object"
                                                :properties {:d {:type "boolean"}}
                                                :additionalProperties false}}}
                       :additionalProperties false}))))

(deftest required-properties
  (is (= {(s/optional-key :a) s/Int
          (s/required-key :b) s/Num}
         (->prismatic {:type "object"
                       :properties {:a {:type "integer"}
                                    :b {:type "number"}}
                       :additionalProperties false
                       :required ["b"]}))))

(deftest additional-properties
  (testing "additional properties not allowed"
    (is (= {(s/optional-key :a) s/Int}
           (->prismatic {:type "object"
                         :properties {:a {:type "integer"}}
                         :additionalProperties false}))))

  (testing "additional properties allowed with no schema"
    (is (= {(s/optional-key :a) s/Int
            s/Str s/Any}
           (->prismatic {:type "object"
                         :properties {:a {:type "integer"}}
                         :additionalProperties true})))
    (is (= {(s/optional-key :a) s/Int
            s/Str s/Any}
           (->prismatic {:type "object"
                         :properties {:a {:type "integer"}}}))))

  (testing "additional properties allowed with schema"
    (is (= {(s/optional-key :a) s/Int
            s/Str s/Int}
           (->prismatic {:type "object"
                         :properties {:a {:type "integer"}}
                         :additionalProperties {:type "integer"}})))
    (is (= {(s/optional-key :a) s/Int
            s/Str {(s/optional-key :b) s/Num}}
           (->prismatic {:type "object"
                         :properties {:a {:type "integer"}}
                         :additionalProperties {:type "object"
                                                :properties {:b {:type "number"}}
                                                :additionalProperties false}})))
    (is (= {(s/optional-key :a) s/Int
            s/Str [s/Bool]}
           (->prismatic {:type "object"
                         :properties {:a {:type "integer"}}
                         :additionalProperties {:type "array"
                                                :items {:type "boolean"}}})))))

(deftest arrays
  (is (= {(s/optional-key :a) [s/Int]}
         (->prismatic {:type "object"
                       :properties {:a {:type "array" :items {:type "integer"}}}
                       :additionalProperties false})))
  (is (= [s/Int]
         (->prismatic {:type "array"
                       :items {:type "integer"}}))))

(deftest enum
  (is (= {(s/optional-key :a) (s/enum "foo" "bar")}
         (->prismatic {:type "object"
                       :properties {:a {:type "string" :enum ["foo" "bar"]}}
                       :additionalProperties false})))
  (is (= {(s/optional-key :a) (s/enum 1 2 3)}
         (->prismatic {:type "object"
                       :properties {:a {:type "integer" :enum [1 2 3]}}
                       :additionalProperties false})))
  (is (= (s/enum 1 2 3)
         (->prismatic {:type "integer" :enum [1 2 3]}))))

(deftest one-of
  (is (= (s/cond-pre s/Str s/Int)
         (->prismatic {:oneOf [{:type "string"} {:type "integer"}]}))))
