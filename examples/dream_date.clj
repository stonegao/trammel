(ns dream-date
  (:use [fogus.me.trammel :only [provide-contracts defconstrainedrecord
                                 all-numbers?]]))

(in-ns 'fogus.me.trammel)

(def *canonical-days* [31 28 31 30 31 30 31 31 30 31 30 31])

(defconstrainedrecord Date [year 1970 month 1 day 1]
  [(every? number? [year month day])
   (< month 13) (> month 0)
   (< day 32) (> day 0)]
  
  Object
  (toString [this]
    (str (:year this) "." (:month this) "." (:day this))))


(defn leap-year?
  [year]
  (and (= (mod year 4) 0)
       (not (some #{(mod year 400)}
                  [100 200 300]))))

(provide-contracts
 [leap-year? "Leap year calculation constraints"
  [y] [number? pos?]])


(defn gregorian-last-day-of-month
  ([date] (gregorian-last-day-of-month (:month date) (:year date)))
  ([month year]
     (if (and (= month 2)
              (leap-year? year))
       29
       (nth *canonical-days* (dec month)))))

(provide-contracts
 [gregorian-last-day-of-month "Gregorian last day calculation constraints"
  [d]   [map? :month :year => number? pos?]
  [m y] [all-positive? => number? pos?]])


(defn days-in-prior-months
  ([date] (days-in-prior-months (:month date) (:year date)))
  ([month year]
     (reduce +
             (map #(gregorian-last-day-of-month % year)
                  (range 1 (inc month))))))

(provide-contracts
 [days-in-prior-months "Prior days calculation constraints"
  [d]   [map? :month :year => number? pos?]
  [m y] [all-positive? => number? pos?]])


(defn days-in-prior-years
  [year]
  (* 365 (dec year)))

(provide-contracts
 [days-in-prior-years "Prior days from year calculation constraints"
  [y] [number? pos?]])


(defn julian-leap-days-in-prior-years
  [year]
  (quot (dec year) 4))

(defn prior-century-years
  [year]
  (quot (dec year) 100))


(defn gregorian->absolute
  ([date] (gregorian->absolute (:day date) (:month date) (:year date)))
  ([day month year]
     (+ day
        (days-in-prior-months month year)
        (days-in-prior-years year)
        (julian-leap-days-in-prior-years year)
        (- (prior-century-years year))
        (quot (dec year) 400))))


(gregorian->absolute {:day 16 :month 12 :year 1999})
(gregorian->absolute (bean (java.util.Date.)))

(-> (java.util.GregorianCalendar. (+ 1999 1899) 12 16)
    (.getTime)
    bean)