package io.github.sahilghorpade.nurl.link.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component
public class QrCodeGenerator {

	public byte[]  generateQR(String shortUrl) {

		try {
			BitMatrix bitMatrix =
					new MultiFormatWriter().encode(
							shortUrl,
							BarcodeFormat.QR_CODE,
							300,
							300
					);

			ByteArrayOutputStream outputStream =
					new ByteArrayOutputStream();

			MatrixToImageWriter.writeToStream(
					bitMatrix,
					"png",
					outputStream
			);

			return outputStream.toByteArray();
		}
		catch(Exception e) {
			throw new RuntimeException(
					"Failed to generate QR code.",
					e
			);
		}

	}
}
