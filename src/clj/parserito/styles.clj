(ns parserito.styles
  (:require [garden.stylesheet :refer [rule]]
            [garden.def :refer [defrule defstyles]]))

(defstyles screen
  [:button :input :optgroup :select :textarea
   {:font-family "monospace"}])

