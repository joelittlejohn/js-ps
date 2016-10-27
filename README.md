# js-ps

Convert JSON schema to Prismatic/Plumatic schema. Supports complex types and arbitrary levels of nesting.

## Usage

Add to your lein dependencies:


![Latest version](https://clojars.org/js-ps/latest-version.svg)

Convert a JSON schema:

```clj
(ns foo
  (:require [js-ps.core :refer [->prismatic]]
            [schema.core :as s]))

(def json-schema
  {:type "object"
   :properties {:a {:type "string"}
                :b {:type "integer"}
                :c {:type "array"
                    :items {:type "boolean"}}}})

(def prismatic-schema
  (->prismatic json-schema)
```

You can use _**local**_ refs in your schema, these are assumed to be resolvable in the given schema:

```clj
(def json-schema
  {:type "object"
   :properties {:a {"$ref" "#/definitions/a"}}
   :definitions {:a {:type "string"}}})

(def prismatic-schema
  (->prismatic json-schema)
```

alternatively, you can pass a parent document against which refs will be resolved:


```clj
(def json-schema
  {:type "object"
   :properties {:a {"$ref" "#/definitions/a"}}})

(def document
  {:definitions {:a {:type "string"}}})

(def prismatic-schema
  (->prismatic json-schema document)
```

## Feature support

This is largely for use with ring-swagger so support for JSON schema features is not intended to be exhaustive. The supported schema rules include:

* additionalProperties
* description
* enum
* items (but not tuples)
* oneOf
* properties
* required
* type (but not union types)
* $ref (but only local refs, by path)

## License

Copyright © 2016 Joe Littlejohn

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
