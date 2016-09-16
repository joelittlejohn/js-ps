(ns js-ps.core
  (:require [clojure.string :as str]
            [schema.core :as s]
            [cheshire.core :as json]))

(declare >additional-properties >array >enum >object >property >schema >type)

(defn- ref->name
  [prefix ref]
  (str prefix (last (str/split ref #"/"))))

(defn- resolve-ref
  [m ref]
  (get-in m (-> ref (subs 2) (str/split #"/") (->> (map keyword)))))

(defn required?
  [property schema]
  (some #{(name property)} (:required schema)))

(defn >property
  [[k v] schema document]
  (if (required? k schema)
    {(s/required-key k) (>schema v document)}
    {(s/optional-key k) (>schema v document)}))

(defn >additional-properties
  [schema document]
  (let [a (:additionalProperties schema)]
    (cond (or (nil? a) (true? a)) {s/Str s/Any}
          (false? a) {}
          :else {s/Str (>schema a document)})))

(defn >object
  [schema document]
  (merge (>additional-properties schema document)
         (when-let [properties (:properties schema)]
           (reduce #(merge %1 (>property %2 schema document)) {} properties))))

(defn >array
  [schema document]
  (if-let [item-schema (:items schema)]
    [(>schema item-schema document)]
    [s/Any]))

(defn >enum
  [schema document]
  (apply s/enum (:enum schema)))

(defn >type
  [schema document]
  (case (:type schema)
    "array"   (>array schema document)
    "boolean" s/Bool
    "integer" s/Int
    "number"  s/Num
    "object"  (>object schema document)
    "string"  s/Str
    "void"    s/Str
    s/Any))

(defn >one-of
  [schema document]
  (->> schema :oneOf (map #(>schema % document)) (apply s/cond-pre)))

(defn >schema
  [schema document]
  (if-let [ref (:$ref schema)]
    (let [resolved-schema (resolve-ref document ref)]
      (s/schema-with-name (>schema resolved-schema document) (ref->name (:title (:info document)) ref)))
    (cond (:enum schema) (>enum schema document)
          (:oneOf schema) (>one-of schema document)
          :else (>type schema document))))

(defn ->prismatic
  "Convert a Clojure representation of a JSON schema into a Prismatic
  schema. Also accepts the root document from which to resolve refs, if
  no root document is passed then all refs are assumed to exist in the
  schema itself."
  ([jsonschema]
   (>schema jsonschema jsonschema))
  ([jsonschema document]
   (>schema jsonschema document)))
