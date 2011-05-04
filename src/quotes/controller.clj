(ns quotes.controller
  (:require [quotes.views :as views]))

(defn index []
  (views/index-html))

(defn quote-form []
  (views/quote-form-html))

(defn quote-form-submit [params]
  (println params) (views/quote-submitted))
(defn browse-quotes []
  (views/browse-quotes-html))
