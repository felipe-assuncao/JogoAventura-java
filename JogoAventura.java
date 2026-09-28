import java.util.ArrayList;
import java.util.Collections;
import java.util.InputMismatchException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;

enum EstadoMissao {
    PENDENTE, CONCLUIDA
}

interface Usavel {
    void usar(Personagem personagem);
}

class ItemNaoEncontradoException extends Exception {
    public ItemNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}

class Inventario<T> {
    private final List<T> itens = new ArrayList<>();

    public void adicionar(T item) {
        itens.add(Objects.requireNonNull(item, "Não é possível adicionar um item nulo."));
    }

    public boolean remover(T item) {
        return itens.remove(item);
    }

    public T buscar(T modelo) throws ItemNaoEncontradoException {
        int index = itens.indexOf(modelo);
        if (index != -1) {
            return itens.get(index);
        }
        throw new ItemNaoEncontradoException("O item solicitado não está no inventário.");
    }

    public List<T> listar() {
        return Collections.unmodifiableList(itens);
    }
}

abstract class Item {
    private final String nome;
    private final int valor;

    public Item(String nome, int valor) {
        this.nome = nome;
        this.valor = valor;
    }

    public String getNome() { return nome; }
    public int getValor() { return valor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(nome, item.nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome);
    }
}

class Pocao extends Item implements Usavel {
    private final int pontosCura;

    public Pocao(String nome, int valor, int pontosCura) {
        super(nome, valor);
        if (pontosCura < 0) {
            throw new IllegalArgumentException("Pontos de cura não podem ser negativos.");
        }
        this.pontosCura = pontosCura;
    }

    @Override
    public void usar(Personagem personagem) {
        int curado = personagem.curar(pontosCura);
        System.out.println("Você bebeu a " + getNome() + " e recuperou " + curado + " de vida!");
    }
}

class Arma extends Item {
    private final int danoAdicional;

    public Arma(String nome, int valor, int danoAdicional) {
        super(nome, valor);
        this.danoAdicional = danoAdicional;
    }

    public int getDanoAdicional() { return danoAdicional; }
}

abstract class Inimigo {
    private final String nome;
    private int vida;
    private final int danoBase;

    public Inimigo(String nome, int vida, int danoBase) {
        if (vida < 0 || danoBase < 0) {
            throw new IllegalArgumentException("Vida e dano base não podem ser negativos.");
        }
        this.nome = nome;
        this.vida = vida;
        this.danoBase = danoBase;
    }

    public String getNome() { return nome; }
    public int getVida() { return vida; }
    public int getDanoBase() { return danoBase; }

    public abstract void atacar(Personagem personagem);

    public void receberDano(int dano) {
        if (dano < 0) {
            throw new IllegalArgumentException("Dano não pode ser negativo.");
        }
        this.vida = Math.max(0, this.vida - dano);
        System.out.println(this.nome + " recebeu " + dano + " de dano. Vida restante: " + this.vida);
    }

    public boolean estaVivo() {
        return vida > 0;
    }
}

class Goblin extends Inimigo {
    public Goblin(String nome, int vida, int danoBase) {
        super(nome, vida, danoBase);
    }

    @Override
    public void atacar(Personagem personagem) {
        System.out.println("O " + getNome() + " salta e ataca agressivamente com uma adaga enferrujada!");
        personagem.receberDano(getDanoBase());
    }
}

class Esqueleto extends Inimigo {
    public Esqueleto(String nome, int vida, int danoBase) {
        super(nome, vida, danoBase);
    }

    @Override
    public void atacar(Personagem personagem) {
        System.out.println("O " + getNome() + " dispara uma flecha de longe com seu arco podre!");
        personagem.receberDano(getDanoBase() + 3);
    }
}

class Personagem {
    private final String nome;
    private int vida;
    private final int vidaMaxima;
    private int ouro;
    private final Inventario<Item> inventario;

    public Personagem(String nome, int vidaMaxima) {
        if (vidaMaxima <= 0) {
            throw new IllegalArgumentException("A vida máxima deve ser maior que zero.");
        }
        this.nome = nome;
        this.vidaMaxima = vidaMaxima;
        this.vida = vidaMaxima;
        this.ouro = 0;
        this.inventario = new Inventario<>();
    }

    public String getNome() { return nome; }
    public int getVida() { return vida; }
    public int getOuro() { return ouro; }
    public Inventario<Item> getInventario() { return inventario; }

    public void atacar(Inimigo inimigo) {
        int dano = 15;
        System.out.println(nome + " atacou " + inimigo.getNome() + " causando " + dano + " de dano.");
        inimigo.receberDano(dano);
    }

    public void receberDano(int dano) {
        if (dano < 0) {
            throw new IllegalArgumentException("Dano não pode ser negativo.");
        }
        this.vida = Math.max(0, this.vida - dano);
        System.out.println(nome + " recebeu " + dano + " de dano. Vida restante: " + this.vida);
    }

    // Retorna quanto de vida foi realmente recuperado (respeitando a vida máxima).
    public int curar(int valor) {
        if (valor < 0) {
            throw new IllegalArgumentException("Cura não pode ser negativa.");
        }
        int curado = Math.min(valor, vidaMaxima - vida);
        this.vida += curado;
        return curado;
    }

    public void adicionarOuro(int valor) {
        if (valor < 0) {
            throw new IllegalArgumentException("Não é possível adicionar ouro negativo.");
        }
        this.ouro += valor;
    }

    public boolean estaVivo() {
        return vida > 0;
    }

    public void mostrarStatus() {
        System.out.println("\n--- Status do Personagem ---");
        System.out.println("Nome: " + nome + " | Vida: " + vida + "/" + vidaMaxima + " | Ouro: " + ouro);
        System.out.print("Itens: ");
        if (inventario.listar().isEmpty()) {
            System.out.println("Nenhum");
        } else {
            for (Item item : inventario.listar()) {
                System.out.print(item.getNome() + " ");
            }
            System.out.println();
        }
        System.out.println("----------------------------");
    }
}

class Missao {
    private final String titulo;
    private final String descricao;
    private EstadoMissao estado;
    private final List<Inimigo> alvos;
    private final Item recompensa;
    private final int ouroRecompensa;

    public Missao(String titulo, String descricao, List<Inimigo> alvos, Item recompensa, int ouroRecompensa) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.alvos = new ArrayList<>(alvos);
        this.recompensa = recompensa;
        this.ouroRecompensa = ouroRecompensa;
        this.estado = EstadoMissao.PENDENTE;
    }

    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public EstadoMissao getEstado() { return estado; }

    public void verificarConclusao(Personagem personagem) {
        if (estado == EstadoMissao.CONCLUIDA) return;

        boolean todosDerrotados = true;
        for (Inimigo alvo : alvos) {
            if (alvo.estaVivo()) {
                todosDerrotados = false;
                break;
            }
        }

        if (todosDerrotados) {
            this.estado = EstadoMissao.CONCLUIDA;
            personagem.adicionarOuro(ouroRecompensa);
            if (recompensa != null) {
                personagem.getInventario().adicionar(recompensa);
            }
            System.out.println("\n[MISSÃO CONCLUÍDA: " + titulo + "]");
            System.out.println("Recompensa: " + ouroRecompensa + " de ouro e "
                    + (recompensa != null ? recompensa.getNome() : "nenhum item") + ".");
        }
    }
}

public class JogoAventura {
    private static final String NOME_POCAO = "Poção de Cura";

    public static void main(String[] args) {
        Personagem heroi = new Personagem("Aventureiro", 60);
        heroi.getInventario().adicionar(new Pocao(NOME_POCAO, 20, 40));

        List<Inimigo> inimigos = new ArrayList<>();
        inimigos.add(new Goblin("Goblin da Floresta", 40, 12));
        inimigos.add(new Esqueleto("Esqueleto Arqueiro", 60, 10));

        Missao missao = new Missao(
                "Limpar a Floresta",
                "Derrote o Goblin e o Esqueleto",
                inimigos,
                new Arma("Espada Longa", 100, 10),
                50);

        System.out.println("=== BEM-VINDO À AVENTURA ===");
        System.out.println("Missão: " + missao.getTitulo() + " - " + missao.getDescricao());

        try (Scanner scanner = new Scanner(System.in)) {
            jogo:
            for (Inimigo inimigoAtual : inimigos) {
                if (!heroi.estaVivo()) break;

                System.out.println("\nUm " + inimigoAtual.getNome() + " apareceu!");

                while (inimigoAtual.estaVivo() && heroi.estaVivo()) {
                    System.out.println("\nEscolha sua ação:");
                    System.out.println("1. Atacar");
                    System.out.println("2. Usar " + NOME_POCAO);
                    System.out.println("3. Ver Status");

                    int escolha;
                    try {
                        escolha = scanner.nextInt();
                    } catch (InputMismatchException e) {
                        System.out.println("Entrada inválida! Digite um número.");
                        scanner.next(); 
                        continue;
                    } catch (NoSuchElementException e) {
                        System.out.println("\nEntrada encerrada. Fim de jogo.");
                        break jogo;
                    }

                    boolean gastouTurno = false;

                    switch (escolha) {
                        case 1:
                            heroi.atacar(inimigoAtual);
                            gastouTurno = true;
                            break;
                        case 2:
                            try {
                                Pocao pocao = (Pocao) heroi.getInventario().buscar(new Pocao(NOME_POCAO, 0, 0));
                                pocao.usar(heroi);
                                heroi.getInventario().remover(pocao);
                                gastouTurno = true;
                            } catch (ItemNaoEncontradoException e) {
                                System.out.println("Erro: " + e.getMessage());
                            }
                            break;
                        case 3:
                            heroi.mostrarStatus();
                            break;
                        default:
                            System.out.println("Ação inválida.");
                    }

                    // Polimorfismo: não importa se é Goblin ou Esqueleto.
                    if (gastouTurno && inimigoAtual.estaVivo()) {
                        inimigoAtual.atacar(heroi);
                    }
                }
            }

            if (heroi.estaVivo()) {
                missao.verificarConclusao(heroi);
            }

            if (missao.getEstado() == EstadoMissao.CONCLUIDA) {
                System.out.println("\nVITÓRIA! Você derrotou todos os inimigos.");
                heroi.mostrarStatus();
            } else if (!heroi.estaVivo()) {
                System.out.println("\nDERROTA! Você sucumbiu aos ferimentos na floresta.");
            }
        }
    }
}
