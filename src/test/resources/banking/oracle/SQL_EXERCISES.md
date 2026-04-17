\# Section 4.5 - SQL Exercises with Expected Outputs

\# Practice these before interviews!



\---



\## Exercise 1: GOLD Customers

Task: Find all GOLD tier customers

Hint: SELECT WHERE tier='GOLD'

Expected: Alice Johnson (CUST-01, credit 780)



\-- Write your SQL here:





\---



\## Exercise 2: Total DELIVERED Revenue

Task: Find total revenue from DELIVERED orders

Hint: SUM WHERE status='DELIVERED'

Expected: 1149.98



\-- Write your SQL here:





\---



\## Exercise 3: Product Never Ordered

Task: Find products that have never been ordered

Hint: NOT IN (SELECT product\_id FROM order\_items)

Expected: PROD-004 Mouse (stock 0)



\-- Write your SQL here:





\---



\## Exercise 4: Average Order Value Per Status

Task: Find average order value grouped by status

Hint: AVG GROUP BY status

Expected:

&#x20; DELIVERED: 1149.98

&#x20; PAID:      299.99

&#x20; CREATED:   999.99



\-- Write your SQL here:





\---



\## Exercise 5: Most Purchased Product

Task: Find product with highest total quantity ordered

Hint: MAX SUM(quantity) from order\_items JOIN products

Expected: PROD-001 Laptop (qty 2 across all orders)



\-- Write your SQL here:





\---



\## Exercise 6: Customers With No Orders

Task: Find customers who have never placed an order

Hint: LEFT JOIN WHERE order\_id IS NULL

Expected: CUST-99 Test User



\-- Write your SQL here:





\---



\## Exercise 7: Orders With kafka\_offset > 1024

Task: Find orders published to Kafka after offset 1024

Hint: WHERE kafka\_offset > 1024

Expected: ORD-1002 (1025), ORD-1004 (1026)



\-- Write your SQL here:





\---



\## Exercise 8: Payment Success Rate

Task: Calculate percentage of COMPLETED payments

Hint: COUNT(COMPLETED) / COUNT(\*) \* 100

Expected: 100% for sample data (2 out of 2)



\-- Write your SQL here:





\---



\## Exercise 9: Products Bought Together

Task: Find products bought in same order

Hint: Self-join order\_items on same order\_id

Expected: Laptop + Headphones on ORD-1001



\-- Write your SQL here:





\---



\## Exercise 10: PLATINUM Customer Spending

Task: Find PLATINUM customers net revenue excluding CANCELLED

Hint: JOIN tier=PLATINUM + SUM excluding CANCELLED

Expected: Carol Davis - 0 net revenue (1 cancelled order)



\-- Write your SQL here:





\---



\## Answer Key (try yourself first!)



Ex 1:

SELECT customer\_id, first\_name, last\_name, credit\_score

FROM customers WHERE tier = 'GOLD';



Ex 2:

SELECT SUM(total\_amount) AS delivered\_revenue

FROM orders WHERE status = 'DELIVERED';



Ex 3:

SELECT product\_id, name, stock\_qty

FROM products

WHERE product\_id NOT IN (SELECT product\_id FROM order\_items);



Ex 4:

SELECT status, ROUND(AVG(total\_amount),2) AS avg\_value

FROM orders

GROUP BY status

ORDER BY avg\_value DESC;



Ex 5:

SELECT p.product\_id, p.name, SUM(oi.quantity) AS total\_qty

FROM order\_items oi

JOIN products p ON p.product\_id = oi.product\_id

GROUP BY p.product\_id, p.name

ORDER BY total\_qty DESC

FETCH FIRST 1 ROWS ONLY;



Ex 6:

SELECT c.customer\_id, c.first\_name, c.last\_name

FROM customers c

LEFT JOIN orders o ON o.customer\_id = c.customer\_id

WHERE o.order\_id IS NULL;



Ex 7:

SELECT order\_id, kafka\_offset, kafka\_partition, status

FROM orders

WHERE kafka\_offset > 1024

ORDER BY kafka\_offset;



Ex 8:

SELECT COUNT(CASE WHEN status='COMPLETED' THEN 1 END) AS completed,

&#x20;      COUNT(\*) AS total,

&#x20;      ROUND(COUNT(CASE WHEN status='COMPLETED' THEN 1 END)/COUNT(\*)\*100,2) AS success\_rate

FROM payments;



Ex 9:

SELECT a.order\_id,

&#x20;      p1.name AS product1,

&#x20;      p2.name AS product2

FROM order\_items a

JOIN order\_items b ON a.order\_id = b.order\_id AND a.item\_id < b.item\_id

JOIN products p1 ON p1.product\_id = a.product\_id

JOIN products p2 ON p2.product\_id = b.product\_id;



Ex 10:

SELECT c.customer\_id, c.first\_name, c.last\_name,

&#x20;      NVL(SUM(CASE WHEN o.status != 'CANCELLED'

&#x20;          THEN o.total\_amount END), 0) AS net\_revenue

FROM customers c

LEFT JOIN orders o ON o.customer\_id = c.customer\_id

WHERE c.tier = 'PLATINUM'

GROUP BY c.customer\_id, c.first\_name, c.last\_name;

