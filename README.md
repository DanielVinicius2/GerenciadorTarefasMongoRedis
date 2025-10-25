# GerenciadorTarefasMongoRedis

Aplicação simples de **gerenciamento de tarefas** desenvolvida em **Java**, utilizando **MongoDB** e **Redis** como bancos de dados.  
O projeto serve como exemplo prático para compreender o uso combinado de um banco NoSQL (MongoDB) e um banco em memória (Redis).

---

## 🧩 Sobre o Projeto

O sistema permite **armazenar, consultar, atualizar e excluir tarefas**, demonstrando operações **CRUD**.  
O **MongoDB** é responsável pelo armazenamento permanente dos dados, enquanto o **Redis** pode ser utilizado para cache, controle rápido de estado ou contagem de acessos.

Esse projeto é ideal para fins didáticos e para compreender a integração entre diferentes bancos NoSQL em uma aplicação Java.

---

## ⚙️ Tecnologias Utilizadas

- **Java** (versão 11 ou superior)
- **MongoDB** (para persistência)
- **Redis** (para cache / armazenamento rápido)
- **Jedis** (biblioteca de integração com Redis)
- **MongoDB Driver** (conexão Java com MongoDB)
- **Maven** (gerenciamento de dependências)

---

## 📂 Estrutura do Projeto

GerenciadorTarefasMongoRedis/
├── src/
│ └── main/
│ └── java/
│ └── gerenciadortarefas/
│ ├── GerenciadorTarefas.java # Classe principal
│ ├── Tarefa.java # Modelo de dados
│ ├── RedisConnection.java # Configuração do Redis
│ ├── MongoConnection.java # Configuração do MongoDB
│ └── ... # Outras classes auxiliares
├── pom.xml # Dependências Maven
└── README.md # Documentação do projeto


---

## 🚀 Como Executar na Sua Máquina

### 🧠 Pré-requisitos

Antes de iniciar, certifique-se de ter instalado:

- [Java JDK 11+](https://www.oracle.com/java/technologies/downloads/)
- [Maven](https://maven.apache.org/download.cgi)
- [MongoDB](https://www.mongodb.com/try/download/community)
- [Redis](https://redis.io/download)

### 🔧 Passos para Configuração

1. **Clone o repositório**
   ```bash
   git clone https://github.com/DanielVinicius2/GerenciadorTarefasMongoRedis.git
   cd GerenciadorTarefasMongoRedis
2. **Configure as conexões**

Verifique as classes `MongoConnection.java` e `RedisConnection.java`.  
Caso necessário, ajuste os parâmetros de conexão:

```java
// MongoDB
MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
MongoDatabase database = mongoClient.getDatabase("gerenciadortarefas");

// Redis
Jedis jedis = new Jedis("localhost", 6379);
jedis.auth("SENHA_DO_REDIS"); // se houver senha

mvn clean install
mvn exec:java -Dexec.mainClass="gerenciadortarefas.GerenciadorTarefas"

redis-cli
keys *
```

### 🧠 Funcionalidades Principais

- Inserir novas tarefas
- Listar todas as tarefas
- Atualizar tarefas existentes
- Excluir tarefas
- Cache de tarefas recentes usando Redis

```
{
  "titulo": "Finalizar relatório",
  "descricao": "Concluir o relatório semanal até sexta-feira",
  "status": "pendente",
  "dataCriacao": "2025-10-24T12:00:00Z"
}
```
