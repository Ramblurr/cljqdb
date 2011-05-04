(ns quotes.model
  "The database model/wrapper for quotes"
  (:require [kyoto-cabinet.core :as kc] [clj-yaml.core :as yaml] [clj-time.core :as time] [clj-time.coerce :as coerce]))

(def db_file "quotes.kch")

(defn- wrap [arg]
  (yaml/generate-string arg))

(defn- unwrap [arg]
  (yaml/parse-string arg))

(defn get-quotes
  "Retrieves quotes from the database.

   The option hash may contain any of the keys below.
   If multiple keys are present, the properties they
   imply must all apply to the resulting quotes.

   id - ID of the quote to retrieve."
  [{:keys [id]}]
  (kc/with-cabinet {:filename "quotes.kch" :mode (+ kc/OREADER) }
    (unwrap (kc/get-value id))))

(defn put-quote
  "Adds a quote to the database

  Expects a map where the keys are:
  :body, :notes, :approved and :tags"
  [{:keys [body notes approved tags] :as all}]
  (kc/with-cabinet {:filename db_file :mode (+ kc/OWRITER kc/OCREATE) }
    (let [id (kc/increment "quotes_id")]
      (kc/put-value id (wrap (assoc all :timestamp (coerce/to-long (time/now))))))))

(defn get-latest
  "Retrieves the n latest quotes from the database."
  [n]
  (kc/with-cabinet {:filename db_file :mode (+ kc/OWRITER ) }
    (let [total (inc (kc/increment "quotes_id" 0))] ; is this the only way to read the value?
        (for [id (range (max 1 (- total n)) total)]
          ; calling this func is overkill, we should just query it
          (get-quotes (assoc {} :id id))))))
