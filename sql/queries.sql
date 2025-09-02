-- Modifying Data
-- Question 1: Insert some data into a table
INSERT INTO cd.facilities
(facid, name, membercost, guestcost, initialoutlay, monthlymaintenance)
values (9, 'Spa', 20, 30, 100000, 800);

-- Question 2: Insert calculated data into a table
INSERT INTO cd.facilities
(facid, name, membercost, guestcost, initialoutlay, monthlymaintenance)
select (select max(facid) from cd.facilities)+1, 'Spa', 20, 30, 100000, 800;

-- Question 3: Update some existing data
UPDATE cd.facilities SET initialoutlay = 10000 WHERE facid = 1;

-- Question 4: Update a row based on the contents of another row
UPDATE cd.facilities facs
SET membercost = (SELECT membercost * 1.1 FROM cd.facilities WHERE facid = 0),
    guestcost = (SELECT guestcost * 1.1 FROM cd.facilities WHERE facid = 0)
WHERE facs.facid = 1;

-- Question 5: Delete all bookings
DELETE FROM cd.bookings;

-- Question 6: Delete a member from the cd.members table
DELETE FROM cd.members WHERE memid = 37;

-- Basics
-- Question 7: Control which rows are retrieved
SELECT facid, name, membercost, monthlymaintenance
FROM cd.facilities
WHERE membercost > 0
	AND (membercost < monthlymaintenance / 50.0);

-- Question 8: Basic string searches
SELECT * FROM cd.facilities WHERE name LIKE '%Tennis%';

-- Question 9: Matching against multiple possible values
SELECT * FROM cd.facilities WHERE facid IN (1,5);

-- Question 10: Working with dates
SELECT memid, surname, firstname, joindate FROM cd.members WHERE joindate >= '2012-09-01';

-- Question 11: Combining results from multiple queries
SELECT surname FROM cd.members
UNION
SELECT name FROM cd.facilities;

-- Join Queries
-- Question 12: Retrieve the start times of members' bookings
SELECT starttime FROM cd.bookings
  INNER JOIN cd.members
    ON cd.members.memid = cd.bookings.memid
WHERE cd.members.firstname = 'David'
  AND cd.members.surname = 'Farrell';

-- Question 13: Work out the start times of bookings for tennis courts
SELECT bks.starttime AS START, facs.name AS NAME
FROM cd.facilities facs
    INNER JOIN cd.bookings bks
        ON facs.facid = bks.facid
WHERE
    facs.name IN ('Tennis Court 2','Tennis Court 1') AND
    bks.starttime >= '2012-09-21' AND
    bks.starttime < '2012-09-22'
ORDER BY bks.starttime;

-- Question 14: Produce a list of all members, along with their recommender (Three joins)
SELECT mems.firstname, mems.surname, recs.firstname, recs.surname
FROM cd.members mems
    LEFT OUTER JOIN cd.members recs
        ON recs.memid = mems.recommendedby
ORDER BY mems.surname, mems.firstname;

-- Question 15: Produce a list of all members who have recommended another member (Three joins)
SELECT DISTINCT recs.firstname AS firstname, recs.surname AS surname
	FROM
		cd.members mems
		INNER JOIN cd.members recs
			ON recs.memid = mems.recommendedby
ORDER BY surname, firstname;

-- Question 16: Produce a list of all members, along with their recommender, using no joins (Subquery and join)
SELECT DISTINCT mems.firstname || ' ' || mems.surname AS member,
    (SELECT recs.firstname || ' ' || recs.surname AS recommender
        FROM cd.members recs
            WHERE recs.memid = mems.recommendedby)                
FROM cd.members mems ORDER BY member;

--Aggregation
-- Question 17: Count the number of recommendations each member makes (Group by order by)
SELECT recommendedby, count(*)
FROM cd.members
WHERE recommendedby IS NOT NULL
GROUP BY recommendedby
ORDER BY recommendedby asc;

-- Question 18: List the total slots booked per facility (Group by order by)
SELECT facid, sum(slots) AS "Total Slots"
FROM cd.bookings
GROUP BY facid
ORDER BY facid;

-- Question 19: List the total slots booked per facility in a given month (group by with condition)
SELECT facid, sum(slots) FROM cd.bookings
WHERE starttime >= '2012-09-01' AND starttime < '2012-10-01'
GROUP BY facid
ORDER BY sum(slots);

-- Question 20: List the total slots booked per facility per month (group by multi col)
SELECT facid, extract(month from starttime) as month, sum(slots) FROM cd.bookings
WHERE extract(year from starttime) = 2012
GROUP BY facid, month
ORDER BY facid, month;

-- Question 21: Find the count of members who have made at least one booking (count distinct)
SELECT COUNT(DISTINCT memid) FROM cd.bookings;

-- Question 22: List each member's first booking after September 1st 2012 (group by multiple cols, join)
SELECT mems.surname, mems.firstname, mems.memid, min(bks.starttime) AS starttime
FROM cd.bookings bks
INNER JOIN cd.members mems ON mems.memid = bks.memid
WHERE starttime >= '2012-09-01'
GROUP BY mems.surname, mems.firstname, mems.memid
ORDER BY mems.memid;

-- Question 23: Produce a list of member names, with each row containing the total member count (Window function)
SELECT COUNT(*) over(), firstname, surname
FROM cd.members
ORDER BY joindate;

-- Question 24: Produce a numbered list of members (window function)
SELECT row_number() over(ORDER BY joindate), firstname, surname
FROM cd.members
ORDER BY joindate;

-- Question 25: Output the facility id that has the highest number of slots booked, again (window function, subquery, group by)
SELECT facid, total FROM (
    SELECT facid, sum(slots) total, rank() over (ORDER BY sum(slots) DESC) rank
        FROM cd.bookings
        GROUP BY facid
    ) AS ranked
    WHERE rank = 1;

-- String
-- Question 26: Format the names of members (format string)
SELECT surname || ', ' || firstname AS name FROM cd.members;

-- Question 27: Find telephone numbers with parentheses (WHERE + string function)
SELECT memid, telephone FROM cd.members WHERE telephone ~ '[()]';

-- Question 28: Count the number of members whose surname starts with each letter of the alphabet (group by, substr)
SELECT substr (mems.surname,1,1) AS letter, COUNT(*) AS COUNT
FROM cd.members mems
GROUP BY letter
ORDER BY letter;