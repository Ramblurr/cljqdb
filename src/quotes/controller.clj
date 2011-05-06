(ns quotes.controller
  (:require [quotes.views :as views] [quotes.model :as model]))

(defn index []
  (views/index-html))

(defn quote-form []
  (views/quote-form-html))

(defn quote-form-submit [params]
  (let [{:keys [quote tags]} params]
    (views/quote-submitted (model/put-quote {:body quote}))))

(defn browse-quotes []
  (views/browse-quotes-html (model/get-latest 100)))
