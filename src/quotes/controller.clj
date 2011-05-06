(ns quotes.controller
  (:require [clojure.string :as string] [quotes.views :as views] [quotes.model :as model]))

(defn index []
  (views/index-html))

(defn quote-form []
  (views/quote-form-html))

(defn quote-form-submit [params]
  (let [{:keys [quote tags]} params]
    (views/quote-submitted (model/put-quote {:body quote :tags (string/split tags #",")}))))

(defn browse-quotes []
  (views/browse-quotes-html (model/get-latest 100)))

(defn quote-view [id]
  (views/quote-view-html (model/get-quotes {:id id})))

(defn votes [id]
  (views/quote-votes (model/get-quotes {:id id})))

(defn put-votes [remote-addr {:keys [id type]}]
  (cond
    (= type "up")
      (views/vote-result (model/vote-up id remote-addr))
    (= type "down")
      (views/vote-result (model/vote-down id remote-addr))))
