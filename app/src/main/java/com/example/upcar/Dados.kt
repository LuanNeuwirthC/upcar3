package com.example.upcar


fun motoristasIniciais(): List<Motorista> = listOf(
    Motorista(
        1, "Lucas Andrade", 4.9, "18:45", 15.0, 128,
        "Honda Civic • Cinza", "ABC1D23",
        "Praça do Centro Cívico", DESTINO_PADRAO, -25.4195, -49.2700
    ),
    Motorista(
        2, "Ana Beatriz", 4.8, "19:00", 15.0, 94,
        "Chevrolet Onix • Preto", "XYZ9W87",
        "Terminal do Boqueirão", DESTINO_PADRAO, -25.4380, -49.2610
    ),
    Motorista(
        3, "Rodrigo Melo", 4.7, "19:15", 15.0, 210,
        "Hyundai HB20 • Branco", "KKK2J34",
        "Praça Rui Barbosa", DESTINO_PADRAO, -25.4250, -49.2800
    )
)