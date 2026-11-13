package com.enveloppe.core.enums;

// Os tipos de conquista do mural. No banco fica o código (SAUDE); na tela, o label (Saúde)
public enum MuralType {
    CARREIRA("Carreira"), // cada valor passa o seu texto de tela entre parênteses
    CASA("Casa"),
    BENS("Carro e bens"),
    DINHEIRO("Dinheiro"),
    ESTUDOS("Estudos"),
    SAUDE("Saúde"),
    FAMILIA("Família"),
    FE("Fé"),
    VIAGEM("Viagem"),
    OUTRO("Outra");

    private final String label; // o texto de tela de cada valor

    MuralType(String label) { // o Java chama sozinho, uma vez para cada valor da lista
        this.label = label;
    }

    public String getLabel() { // MuralType.SAUDE.getLabel() → "Saúde" | MuralType.SAUDE.name() → "SAUDE"
        return label;
    }
}
