(ns quotes.model
  "The database model/wrapper for quotes"
  (:require [kyoto-cabinet.core :as kc] [clj-yaml.core :as yaml] [clj-time.core :as time] [clj-time.coerce :as coerce]))

(def quotes_db "quotes.kct")
(def tags_db "tags.kct")
(def votes_db "votes.kch")

(defn- wrap [arg]
  (yaml/generate-string arg))

(defn- unwrap [arg]
  (yaml/parse-string arg))

(defn- now []
  (coerce/to-long (time/now)))

(defn add-tag
  "Assigns a quote to a tag."
  [tag id]
  (kc/with-cabinet {:filename tags_db :mode (+ kc/OWRITER kc/OCREATE kc/OREADER)}
    (let [entry (kc/get-value tag)]
      (if entry
        (kc/put-value tag (wrap (conj (unwrap entry) id)))
        (kc/put-value tag (wrap (vector id)))))))

(defn get-tags
  "Get ids for tags"
  [tag]
  (kc/with-cabinet {:filename tags_db :mode (+ kc/OREADER)}
    (unwrap (kc/get-value tag))))

(defn get-quotes
  "Retrieves quotes from the database.

   The option hash may contain any of the keys below.
   If multiple keys are present, the properties they
   imply must all apply to the resulting quotes.

   id - ID of the quote to retrieve."
  [{:keys [id]}]
  (kc/with-cabinet {:filename quotes_db :mode (+ kc/OREADER) }
    (assoc (unwrap (kc/get-value id)) :id id)))

(defn put-quote
  "Adds a quote to the database

  Expects a map where the keys are:
  :body, :notes, :approved and :tags"
  [{:keys [body notes approved tags] :as all}]
  (kc/with-cabinet {:filename quotes_db :mode (+ kc/OWRITER kc/OCREATE) }
    (let [id (kc/increment "quotes_id")]
      (kc/put-value id (wrap (assoc all :up 0 :down 0 :timestamp (now))))
      (map #(add-tag % id) tags))))

(defn- update-quote
  [quote]
  (kc/with-cabinet {:filename quotes_db :mode (+ kc/OWRITER kc/OCREATE) }
    (kc/put-value (get quote :id) (wrap quote))
    (map #(add-tag % (get quote :id)) (get quote :tags))))

(defn get-latest
  "Retrieves the n latest quotes from the database."
  [n]
  (kc/with-cabinet {:filename quotes_db :mode (+ kc/OWRITER ) }
    (let [total (inc (kc/increment "quotes_id" 0))] ; is this the only way to read the value?
        (for [id (reverse (range (max 1 (- total n)) total))]
          ; calling this func is overkill, we should just query it
          (get-quotes (assoc {} :id id))))))



(defn- commit-vote
  [id key]
  (let [aquote (get-quotes (assoc {} :id id))]
    (if (nil? (aquote key))
      (update-quote (assoc aquote key 1))
      (update-quote (assoc aquote key (inc (aquote key)))))))

(defn- commit-down-vote
  [id]
  (commit-vote id :down))

(defn- commit-up-vote
  [id]
  (commit-vote id :up))

(defn- can-vote
  [entry key]
  (if (nil? entry)
    true
    (let [vote (unwrap entry)]
      (if (map? vote)
        (= (vote "type") (name key))
        false))))

(defn- attempt-vote
  [id ipaddress updown]
  (kc/with-cabinet {:filename votes_db :mode (+ kc/OWRITER kc/OCREATE kc/OREADER)}
    (let [key (str ipaddress id) entry (kc/get-value key)]
      (cond
        (nil? entry)
          (do (kc/put-value key (wrap {"type" updown "timestamp" (now)}))
              (commit-vote id updown) true)
        (can-vote entry updown)
          (do (kc/put-value key (wrap (assoc (unwrap entry) "type" updown "timestamp" (now))))
              (commit-vote id updown) true)
        true false))))

(defn vote-up
  "Up vote a quote"
  [id ipaddress]
  (attempt-vote id ipaddress :u))

(defn vote-down
  "Down vote a quote"
  [id ipaddress]
  (attempt-vote id ipaddress :d))
