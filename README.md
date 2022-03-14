# rendezvous
This example was demonstrated in my [ZIO World Conference](https://zioworld.com/) Talk.

Steps to test this application locally

1. Run `docker-compose up`
2. Run the server `sbt run`
3. Open `index.html` file in your browser.
4. Fill the form with a valid name (contains only letters with more than 2 characters) and a valid email.
5. When the QRCode is displayed it will be automatically stored as `images/participantId.png`.
6. Check-in using the QRCode in images folder.
