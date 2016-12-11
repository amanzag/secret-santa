<!DOCTYPE html>

<html lang="en">
<head>
    <title>Amigo Invisible</title>
    <script
        src="https://code.jquery.com/jquery-3.1.1.min.js"
        integrity="sha256-hVVnYaiADRTO2PzUGmuLJr8BLUSjGIZsDYGmIJLv2b8="
        crossorigin="anonymous"></script>
    <link rel="stylesheet" type="text/css" href="/main.css" />
</head>

<body>
<div id="top-section">
    Hola ${user.name}. Te ha tocado participar en el 
    <h1>amigo invisible</h1>
    de <u>${user.game.name}</u> que se va a celebrar el día ${user.game.date}.
</div>
<div id="reveal-section">
    Para saber quien es tu amigo invisible
    <br/>
    <button class="chbutton" id="reveal">Pincha aquí</button>
</div>

<div id="secret-section">
    Te ha tocado comprarle un regalo a este/a:
    <br/>
    <img src="/secret-santa/${user.game.id}/users/${user.giftReceiver.id}/photo" 
        alt="${user.giftReceiver.name}" title="${user.giftReceiver.name}">
    <br/>
    <div class="info">
        El precio maximo del regalo es ${user.game.maxPrice}.
    </div>
</div>

<script>
    $('button#reveal').click(function() {
        $('#reveal-section').hide();
        $('#secret-section').show(1000);
    });
</script>
</body>

</html>