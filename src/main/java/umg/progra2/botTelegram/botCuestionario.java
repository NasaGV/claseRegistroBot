package umg.progra2.botTelegram;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import umg.progra2.model.Respuesta;
import umg.progra2.model.User;
import umg.progra2.service.RespuestaService;
import umg.progra2.service.UserService;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class botCuestionario extends TelegramLongPollingBot {

    private Map<Long, String> estadoConversacion = new HashMap<>();
    User usuarioConectado = null;
    UserService userService = new UserService();

    private final Map<Long, Integer> indicePregunta = new HashMap<>();
    private final Map<Long, String> seccionActiva = new HashMap<>();
    private final Map<String, String[]> preguntas = new HashMap<>();

    public botCuestionario() {

        preguntas.put("SECTION_1", new String[]{"🙀️1.1- Que haces?", "😎 1.2- ya comiste?", "😶‍🌫️1.3- Miras anime? "});
        preguntas.put("SECTION_2", new String[]{"🙂2.1- Has visto one piece", " 😉2.2- te gusta el rock?", "😛2.3- Has jugado Zelda"});
        preguntas.put("SECTION_3", new String[]{"🤩3.1- Xbox o play?", "🤗3.2- Te gusta mortal combat", "3.3- tienes una switch?"});
        preguntas.put("SECTION_4", new String[]{"4.1- Eres elden Lord?", "4.2- Cual es tu edad?", "4.3- En que dispositivo juegas?", "4.4- pepsi o coca?"});
    }

    @Override
    public String getBotUsername() {
        return "@GiggyMeteor_bot";
    }

    @Override
    public String getBotToken() {
        return "6435269698:AAEr88JYiXKqlqbAfDLRId7uJHSI3EkopGc";
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String messageText = update.getMessage().getText();
                long chatId = update.getMessage().getChatId();
                String userFirstName = update.getMessage().getFrom().getFirstName();
                String userLastName = update.getMessage().getFrom().getLastName();
                String nickName = update.getMessage().getFrom().getUserName();

                // Verifica si el usuario esta registrado en el sistema
                String state = estadoConversacion.getOrDefault(chatId, "");
                usuarioConectado = userService.getUserByTelegramId(chatId);

                if (usuarioConectado == null) {
                    // Proceso de registro
                    if (state.isEmpty()) {
                        sendText(chatId, "Hola " + formatUserInfo(userFirstName, userLastName, nickName) + ", Tu usuuario no esa registrado en el sistema. Por favor ingresa tu correo electronico:");
                        estadoConversacion.put(chatId, "ESPERANDO_CORREO...");
                        return;
                    } else if (state.equals("ESPERANDO_CORREO...")) {
                        processEmailInput(chatId, messageText);
                        return;
                    }
                } else {
                    // si el usuario ya esta registrado manejo de cuestionario
                    if (messageText.equals("/menu")) {
                        sendMenu(chatId);
                        return;
                    } else if (seccionActiva.containsKey(chatId)) {
                        enviarRespuesta(seccionActiva.get(chatId), indicePregunta.get(chatId), messageText, chatId);
                        manejaCuestionario(chatId, messageText);
                        return;
                    } else {
                        sendText(chatId, "Hola " + formatUserInfo(userFirstName, userLastName, nickName) + ", envía '/menu' para iniciar el cuestionario. 🙂");
                    }
                }
            } else if (update.hasCallbackQuery()) {
                // Manejo de respuestas de botones
                String callbackData = update.getCallbackQuery().getData();
                long chatId = update.getCallbackQuery().getMessage().getChatId();
                inicioCuestionario(chatId, callbackData);
            }
        } catch (Exception e) {
            long chatId = update.getMessage().getChatId();
            sendText(chatId, "Ocurrió un error al procesar tu mensaje 😵‍💫. Por favor intenta nuevamente.");
            e.printStackTrace();
        }
    }

    private void sendMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Selecciona una sección:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // para crear los botones del menu
        rows.add(crearFilaBoton("Sección 1", "SECTION_1"));
        rows.add(crearFilaBoton("Sección 2", "SECTION_2"));
        rows.add(crearFilaBoton("Sección 3", "SECTION_3"));
        rows.add(crearFilaBoton("Sección 4", "SECTION_4"));

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private List<InlineKeyboardButton> crearFilaBoton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);
        return row;
    }

    private void inicioCuestionario(long chatId, String section) {
        seccionActiva.put(chatId, section);
        indicePregunta.put(chatId, 0);
        enviarPregunta(chatId);
    }

    private void enviarPregunta(long chatId) {
        String seccion = seccionActiva.get(chatId);
        int index = indicePregunta.get(chatId);
        String[] questions = preguntas.get(seccion);

        if (index < questions.length) {
            sendText(chatId, questions[index]);
        } else {
            sendText(chatId, "¡Has completado el cuestionario!");
            seccionActiva.remove(chatId);
            indicePregunta.remove(chatId);
        }
    }

    private void manejaCuestionario(long chatId, String response) {
        String section = seccionActiva.get(chatId);
        int index = indicePregunta.get(chatId);
        if (indicePregunta.get(chatId) == 1) {
            int intresponse = Integer.parseInt(response);
            if (intresponse < 5) {
                sendText(chatId, "Tu respuesta fue: " + response);
                sendText(chatId, "Eres demasiado joven para estar en Telegram sal de aqui🙀\nPor favor, ingresa otra edad si te equivocaste.");
                enviarPregunta(chatId);
            } else if (intresponse > 95) {
                sendText(chatId, "Tu respuesta fue: " + response);
                sendText(chatId, "¿acaso eres una momia andante?.🤨\nPor favor, ingresa otra edad.");
                enviarPregunta(chatId);
            } else {
                siguientepregunta(chatId, response, index);
            }
        } else {
            siguientepregunta(chatId, response, index);
        }
    }

    private void enviarRespuesta(String seccion, Integer preguntaid, String response, Long telegramid) {
        RespuestaService respuestaService = new RespuestaService();
        Respuesta respuesta = new Respuesta();

        respuesta.setSeccion(seccion);
        respuesta.setPreguntaId(preguntaid);
        respuesta.setRespuestaTexto(response);
        respuesta.setTelegramId(telegramid);

        try {
            respuestaService.createRespuesta(respuesta);
            System.out.println("Respuesta creada exitosamente.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void siguientepregunta(long chatId, String response, int index) {
        sendText(chatId, "Tu respuesta fue: " + response);
        indicePregunta.put(chatId, index + 1);
        enviarPregunta(chatId);
    }

    private String formatUserInfo(String firstName, String lastName, String userName) {
        return firstName + " " + lastName;
    }

    private void processEmailInput(long chatId, String email) {
        sendText(chatId, "Tu correo a sido recibido " + email);
        estadoConversacion.remove(chatId);
        try {
            usuarioConectado = userService.getUserByEmail(email);
        } catch (Exception e) {
            System.err.println("ha habido un error al obtener el usuario por tu correo " + e.getMessage());
            e.printStackTrace();
        }
        if (usuarioConectado == null) {
            sendText(chatId, "Tu correo no se encuentra en el sistema, por favor contactate el administrador.");
        } else {
            usuarioConectado.setTelegramid(chatId);
            try {
                userService.updateUser(usuarioConectado);
            } catch (Exception e) {
                System.err.println(" " + e.getMessage());
                e.printStackTrace();
            }
            sendText(chatId, "Tu usuario se a actualizado exitosamente.");
        }
    }

    public void sendText(Long who, String what) {
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString())
                .text(what).build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}

