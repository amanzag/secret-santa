<!DOCTYPE html>

<html lang="en">
<head>
    <link rel="stylesheet" type="text/css" href="/main.css" />
</head>

<body>
Hola ${user.name}. Te ha tocado participar en el 
<h1>amigo invisible</h1> de ${user.game.name} que se va a celebrar el día ${user.game.date}.
<br>
Para saber quien es tu amigo invisible
<br>
<button class="chbutton">Pincha aquí</button>
<br>

Te ha tocado comprarle un regalo a <strong>${user.giftReceiver.name}</strong>
<br>
El precio maximo del regalo es ${user.game.maxPrice}
<br>
<img src="/secret-santa/${user.game.id}/users/${user.id}/photo">
</body>

</html>