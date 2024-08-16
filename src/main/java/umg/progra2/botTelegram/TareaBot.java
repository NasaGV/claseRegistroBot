package umg.progra2.botTelegram;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class TareaBot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return "GiggyMeteor_bot";
    }

    @Override
    public String getBotToken() {
        return "6435269698:AAEr88JYiXKqlqbAfDLRId7uJHSI3EkopGc";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            System.out.println("Mensaje recibido de " + update.getMessage().getFrom().getUserName() + ": " + messageText);

            if (messageText.startsWith("/info")) {
                sendText(chatId, "Nombre: Gabriel Enrique Villanueva Hernandez\nCarnet: 0905-23-21427\nSemestre: 4to");

            } else if (messageText.startsWith("/progra")) {
                sendText(chatId, "La clase de programacion fue muy interesante  y fue muy divertido saber como poder crear nuestros propios bots para telegram.");

            } else if (messageText.startsWith("/hola")) {
                LocalDate fecha = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'del año ' YYYY ", new Locale("es", "ES"));
                String Fecha = fecha.format(formatter);
                sendText(chatId, "Hola amigo " + update.getMessage().getFrom().getFirstName() + " 😉, hoy es " + Fecha);

            } else if (messageText.startsWith("/cambio")) {
                try {
                    String[] parts = messageText.split(" ");
                    double euros = Double.parseDouble(parts[1]);
                    double quetzales = euros * 8.55;
                    sendText(chatId, euros + " euros son " + quetzales + " quetzales.");
                } catch (Exception e) {
                    sendText(chatId, "Por favor usa el formato /cambio <cantidad_en_euros> para lograr hacer el cambio 😉");
                }

            } else if (messageText.startsWith("/grupal")) {
                String groupMessage = messageText.substring(8);
                List<Long> chatIds = List.of(1455734475L, 6710213754L, 6833888660L, 6957944438L);
                for (Long id : chatIds) {
                    sendText(id, groupMessage);
                }
            } else {
                sendText(chatId, "Comando no reconocido. Los comandos disponibles son: \n/info\n/progra\n/hola\n/cambio\n/grupal");
            }
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
