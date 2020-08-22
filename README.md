# ReFresh
An Android application that helps users to easily track which foods are available at home to reduce food waste and save money.
**Features include:**
* Object recognition to identify which foods are available
* Notifications for expiring items
* Recipe generation based on available foods
* Grocery shopping list with up-to-date prices


**Software Architectures Used:**
* Client-server
* Layered architecture (to promote separation of concerns, i.e. presentation, business, persistence, database)
* Event-based architecture (for notifications)


**Design Patterns Used:**
* Strategy (for different image labelling services, e.g. Firebase, MS)
* Facade (to simplify working with external APIs)
