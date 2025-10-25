package gerenciadortarefasmongoredis;

import com.mongodb.client.*;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import redis.clients.jedis.Jedis; // <-- Importa o Jedis (Redis)
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Scanner;

public class GerenciadorTarefas {
    private static final Scanner scanner = new Scanner(System.in);
    private static MongoCollection<Document> gerenciador_tarefas;

    // 🔹 Conexão com o Redis
    private static Jedis redis;

    public static void main(String[] args) {
        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.SEVERE);

        conectarBanco();     // MongoDB
        conectarRedis();     // Redis

        exibirMenu();
    }

    /**
     * Conecta ao MongoDB.
     */
    private static void conectarBanco() {
        String url = "mongodb+srv://Daniel_Vinicius:5mqXufczAMNUKzS1@cluster0.faifbpt.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
        MongoClient conexao = MongoClients.create(url);
        MongoDatabase baseDeDados = conexao.getDatabase("FSA");
        gerenciador_tarefas = baseDeDados.getCollection("gerenciador_tarefas");
        System.out.println("✅ Conectado ao MongoDB!");
    }

/**
 * Conecta ao Redis Cloud.
 */
private static void conectarRedis() {
    // Substitua pelos dados do seu Redis Cloud
    String redisHost = "redis-16354.c81.us-east-1-2.ec2.redns.redis-cloud.com"; // host do Redis Cloud
    int redisPort = 16354; // porta do Redis Cloud
    String redisSenha = "root"; // senha do Redis Cloud

    redis = new Jedis(redisHost, redisPort);
    redis.auth(redisSenha); // autenticação
    System.out.println("✅ Conectado ao Redis Cloud!");
    System.out.println("🔍 Chaves atuais no Redis: " + redis.keys("*"));

}
    private static void exibirMenu() {
        int opcao;
        do {
            System.out.println("\n=== MENU GERENCIADOR DE TAREFAS ===");
            System.out.println("1 - Cadastrar Tarefa");
            System.out.println("2 - Listar Todas as Tarefas");
            System.out.println("3 - Buscar Tarefa por Título");
            System.out.println("4 - Atualizar Tarefa");
            System.out.println("5 - Remover Tarefa");
            System.out.println("6 - Limpar Cache Redis");
            System.out.println("0 - Sair");
            System.out.print("Escolha uma opcao: ");

            opcao = Integer.parseInt(scanner.nextLine());

            switch (opcao) {
                case 1 -> cadastrarTarefa();
                case 2 -> listarTarefas();
                case 3 -> buscarPorTitulo();
                case 4 -> atualizarTarefa();
                case 5 -> removerTarefa();
                case 6 -> limparCacheRedis();
                case 0 -> System.out.println("Saindo... Ate logo!");
                default -> System.out.println("Opção invalida. Tente novamente.");
            }

        } while (opcao != 0);
    }

    private static void cadastrarTarefa() {
        System.out.println("\n=== Cadastro de Tarefa ===");

        System.out.print("Titulo: ");
        String titulo = scanner.nextLine();

        System.out.print("Descricao: ");
        String descricao = scanner.nextLine();

        System.out.print("Prioridade (Baixa, Media, Alta): ");
        String prioridade = scanner.nextLine();

        System.out.print("Status (Pendente, Em Andamento, Concluida): ");
        String status = scanner.nextLine();

        Document tarefa = new Document("Titulo", titulo)
                .append("Descricao", descricao)
                .append("Prioridade", prioridade)
                .append("Status", status);

        gerenciador_tarefas.insertOne(tarefa);
        redis.del("tarefas_cache"); // limpa cache de listagem
        System.out.println("✅ Tarefa cadastrada com sucesso!");
    }

    private static void listarTarefas() {
        System.out.println("\n=== Lista de Tarefas ===");

        // 🔹 Verifica se existe cache
        if (redis.exists("tarefas_cache")) {
            System.out.println("(Usando cache do Redis)");
            System.out.println(redis.get("tarefas_cache"));
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (Document doc : gerenciador_tarefas.find()) {
            sb.append("📝 Titulo: ").append(doc.getString("Titulo")).append("\n")
              .append("📄 Descricao: ").append(doc.getString("Descricao")).append("\n")
              .append("🔥 Prioridade: ").append(doc.getString("Prioridade")).append("\n")
              .append("⚙️ Status: ").append(doc.getString("Status")).append("\n")
              .append("--------------------------\n");
        }

        // 🔹 Armazena no Redis por 60 segundos
        redis.setex("tarefas_cache",3600,sb.toString());

        System.out.println(sb);
    }

    private static void buscarPorTitulo() {
        System.out.print("\nDigite o titulo da tarefa: ");
        String termo = scanner.nextLine();

        // 🔹 Tenta buscar no cache primeiro
        String cacheKey = "tarefa_" + termo.toLowerCase();
        if (redis.exists(cacheKey)) {
            System.out.println("(Usando cache do Redis)");
            System.out.println(redis.get(cacheKey));
            return;
        }

        FindIterable<Document> resultados = gerenciador_tarefas.find(
                new Document("Titulo", new Document("$regex", termo).append("$options", "i"))
        );

        StringBuilder sb = new StringBuilder();
        for (Document doc : resultados) {
            sb.append("📝 Titulo: ").append(doc.getString("Titulo")).append("\n")
              .append("📄 Descricao: ").append(doc.getString("Descricao")).append("\n")
              .append("🔥 Prioridade: ").append(doc.getString("Prioridade")).append("\n")
              .append("⚙️ Status: ").append(doc.getString("Status")).append("\n");
        }

        String resultado = sb.toString();
        if (resultado.isEmpty()) {
            System.out.println("Nenhuma tarefa encontrada.");
        } else {
            redis.setex(cacheKey, 3600, resultado); // guarda por 60s
            System.out.println(resultado);
        }
    }

    private static void atualizarTarefa() {
        System.out.print("\nDigite o titulo da tarefa que deseja atualizar: ");
        String titulo = scanner.nextLine();

        Document filtro = new Document("Titulo", new Document("$regex", titulo).append("$options", "i"));
        Document tarefa = gerenciador_tarefas.find(filtro).first();

        if (tarefa == null) {
            System.out.println("Tarefa nao encontrada!");
            return;
        }

        System.out.print("\nNovo status: ");
        String novoStatus = scanner.nextLine();

        if (!novoStatus.isEmpty()) tarefa.put("Status", novoStatus);

        gerenciador_tarefas.replaceOne(filtro, tarefa);
        redis.del("tarefas_cache"); // limpa cache
        System.out.println("✅ Tarefa atualizada e cache limpo!");
    }

    private static void removerTarefa() {
        System.out.print("\nDigite o titulo da tarefa que deseja remover: ");
        String titulo = scanner.nextLine();

        Document filtro = new Document("Titulo", new Document("$regex", titulo).append("$options", "i"));
        DeleteResult resultado = gerenciador_tarefas.deleteOne(filtro);

        if (resultado.getDeletedCount() > 0) {
            redis.del("tarefas_cache");
            redis.del("tarefa_" + titulo.toLowerCase());
            System.out.println("🗑️ Tarefa removida com sucesso!");
        } else {
            System.out.println("Nenhuma tarefa encontrada com esse titulo.");
        }
    }

    private static void limparCacheRedis() {
        redis.flushAll();
        System.out.println("🧹 Cache Redis limpo com sucesso!");
    }
}

