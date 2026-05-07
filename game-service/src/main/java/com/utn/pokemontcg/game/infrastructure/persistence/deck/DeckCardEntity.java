package com.utn.pokemontcg.game.infrastructure.persistence.deck;

import jakarta.persistence.*;

@Entity
@Table(name = "deck_cards")
public class DeckCardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "deck_id")
    private DeckEntity deck;

    @Column(nullable = false)
    private String cardId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private String type;

    @Column
    private String subtype;

    @Column(nullable = false)
    private boolean basicEnergy;

    @Column(nullable = false)
    private boolean basicPokemon;

    @Column(nullable = false)
    private boolean aceSpec;

    public Long getId() { return id; }
    public DeckEntity getDeck() { return deck; }
    public void setDeck(DeckEntity deck) { this.deck = deck; }
    public String getCardId() { return cardId; }
    public void setCardId(String cardId) { this.cardId = cardId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSubtype() { return subtype; }
    public void setSubtype(String subtype) { this.subtype = subtype; }
    public boolean isBasicEnergy() { return basicEnergy; }
    public void setBasicEnergy(boolean basicEnergy) { this.basicEnergy = basicEnergy; }
    public boolean isBasicPokemon() { return basicPokemon; }
    public void setBasicPokemon(boolean basicPokemon) { this.basicPokemon = basicPokemon; }
    public boolean isAceSpec() { return aceSpec; }
    public void setAceSpec(boolean aceSpec) { this.aceSpec = aceSpec; }
}
