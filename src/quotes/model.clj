(ns quotes.model
  "The database model/wrapper for quotes"
  (:require [kyoto-cabinet.core :as kc] [clj-yaml.core :as yaml]))

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
  (kc/with-cabinet {:filename "quotes.kch" :mode (+ kc/OWRITER kc/OCREATE) }
    (let [id (kc/increment "quotes_id")]
      (kc/put-value id (wrap all)))))
