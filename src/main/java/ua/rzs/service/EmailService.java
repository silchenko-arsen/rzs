package ua.rzs.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ua.rzs.model.Order;

@Service
@RequiredArgsConstructor
public class EmailService {
  private final JavaMailSender mailSender;

  public void sendIbanDetails(String to, String iban, Order order) {
    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setTo(to);
    msg.setSubject("Ваше замовлення схвалено — реквізити для оплати");
    msg.setText(
      "Вітаємо! Ваше замовлення схвалено на послугу:" + order.getServiceItem().getName() + "\n\n" +
      "Будь ласка, здійсніть оплату " + order.getServiceItem().getPrice() + " грн. " + "на наступні реквізити:\n\n" +
      "IBAN: " + iban + "\n\n" +
      "Після оплати надішліть нам підтвердження переказу на цю пошту."
    );
    mailSender.send(msg);
  }

  public void sendRejectionNotice(String to, Order order) {
    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setTo(to);
    msg.setSubject("Ваше замовлення відхилено на послугу: " + order.getServiceItem().getName());
    msg.setText("На жаль, ваше замовлення було відхилено.");
    mailSender.send(msg);
  }
}
