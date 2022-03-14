function subscribe() {

   const url = "http://localhost:9000/rendezvous";
   const name = document.getElementById('name').value
   const email = document.getElementById('email').value
   const body = {
     "name": name,
     "email": email
   }
   const invalid = "invalid";
   const notFoundImage = "images/notfound.png";
   fetch(url, {
     method: 'post',
     body: JSON.stringify(body),
     mode: 'cors'
   })
   .then(response => response.text())
   .then(data => verify_path(data));

function verify_path(path) {
  fetch(path).then(r => {
    return show_image("Spot booked!", "zio!", path, 500, 500, "QRCode")
    })
  .catch(error => show_image("Sorry", "Weird error:" + path, notFoundImage, 500, 500, "Sorry"));
}


function show_image(title, text, src, w, h, alt) {
   document.getElementById('name').value = ""
   document.getElementById('email').value = ""
   const myWindow = window.open("", "RendezVous", 'width=w,height=h');
   myWindow.document.write('<center><h1>'+title+'</h1><br><img src='+src+'></src><h2>'+text+'</h2></center>');
}
}

function checkin() {
  const path = "images/" + document.getElementById("path").files[0].name // very bad.. just to make it work
  // DON'T LOOK UP
  const successImg = "images/successImg.png"
  const failImg = "images/failImg.png"
  console.log('the path is: '+path)
  const url = "http://localhost:9000/rendezvous/infoQR?path="+path;
  const invalidImg = "images/notfound.png";
  fetch(url, {method: "get", mode: 'cors'})
    .then(response => {
      if (!response.ok) throw response.error;
      else return response.json();
    })
    .then(data => {
      const name = data.name
      show_image("QRCode is valid", "Welcome " + name + " to ZIO World!", successImg, 500, 500)
     })
    .catch(error => show_image("Invalid QRCode!", "Make sure you have been already subscribed!", failImg, 500, 500));


  function show_image(title, text, src, w, h, alt) {
     document.getElementById('path').value = ""
     const myWindow = window.open("", "Check-In", 'width=w,height=h');
     myWindow.document.write('<center><h1>'+title+'</h1><br><img src='+src+'></src><h2>'+text+'</h2></center>');
  }
}
