(defproject js-ps "0.1.1"

  :description "Convert JSON schema to Prismatic/Plumatic schema"

  :url "https://github.com/joelittlejohn/js-ps"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [prismatic/schema "1.1.3"]]

  :profiles {:dev {:dependencies [[cheshire "5.6.3"]]}}

  :deploy-repositories [["releases" :clojars]])
