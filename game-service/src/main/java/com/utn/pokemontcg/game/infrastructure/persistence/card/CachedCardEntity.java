package com.utn.pokemontcg.game.infrastructure.persistence.card;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "cached_cards")
public class CachedCardEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private String cardId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String setId;

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

    @Column
    private Integer hp;

    @Column
    private Integer attackDamage;

    @Column
    private Integer attackRequiredEnergy;

    @Column(nullable = false)
    private Instant cachedAt;

    public String getCardId() { return cardId; }
    public void setCardId(String cardId) { this.cardId = cardId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSetId() { return setId; }
    public void setSetId(String setId) { this.setId = setId; }
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
    public Integer getHp() { return hp; }
    public void setHp(Integer hp) { this.hp = hp; }
    public Integer getAttackDamage() { return attackDamage; }
    public void setAttackDamage(Integer attackDamage) { this.attackDamage = attackDamage; }
    public Integer getAttackRequiredEnergy() { return attackRequiredEnergy; }
    public void setAttackRequiredEnergy(Integer attackRequiredEnergy) { this.attackRequiredEnergy = attackRequiredEnergy; }
    public Instant getCachedAt() { return cachedAt; }
    public void setCachedAt(Instant cachedAt) { this.cachedAt = cachedAt; }
}
