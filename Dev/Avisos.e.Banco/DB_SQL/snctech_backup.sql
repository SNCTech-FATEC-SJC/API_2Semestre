--
-- PostgreSQL database dump
--

\restrict OV1DgoeL8dVbPgFm5X8mXCIgHwg6F0t01JIr6LC4vKBWaCClrqbTzRkYQ1T7Xgx

-- Dumped from database version 18.6
-- Dumped by pg_dump version 18.6

-- Started on 2026-10-24 16:25:04

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 868 (class 1247 OID 16390)
-- Name: perfil_usuario; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.perfil_usuario AS ENUM (
    'professor',
    'aluno',
    'admin'
);


ALTER TYPE public.perfil_usuario OWNER TO postgres;

--
-- TOC entry 874 (class 1247 OID 16408)
-- Name: status_tentativa; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.status_tentativa AS ENUM (
    'em_andamento',
    'enviada',
    'nao_realizada_prazo'
);


ALTER TYPE public.status_tentativa OWNER TO postgres;

--
-- TOC entry 871 (class 1247 OID 16398)
-- Name: tipo_questao; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.tipo_questao AS ENUM (
    'multipla_escolha',
    'associacao',
    'discursiva',
    'verdadeiro_falso'
);


ALTER TYPE public.tipo_questao OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 228 (class 1259 OID 16488)
-- Name: alternativas; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.alternativas (
    id integer NOT NULL,
    questao_id integer,
    texto text NOT NULL,
    correta boolean DEFAULT false,
    par_associacao character varying(255)
);


ALTER TABLE public.alternativas OWNER TO postgres;

--
-- TOC entry 227 (class 1259 OID 16487)
-- Name: alternativas_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.alternativas_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alternativas_id_seq OWNER TO postgres;

--
-- TOC entry 5133 (class 0 OID 0)
-- Dependencies: 227
-- Name: alternativas_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.alternativas_id_seq OWNED BY public.alternativas.id;


--
-- TOC entry 231 (class 1259 OID 16530)
-- Name: avaliacao_questoes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.avaliacao_questoes (
    avaliacao_id integer NOT NULL,
    questao_id integer NOT NULL,
    ordem integer
);


ALTER TABLE public.avaliacao_questoes OWNER TO postgres;

--
-- TOC entry 230 (class 1259 OID 16505)
-- Name: avaliacoes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.avaliacoes (
    id integer NOT NULL,
    turma_id integer,
    titulo character varying(150) NOT NULL,
    peso numeric(5,2) DEFAULT 1.0 NOT NULL,
    versao integer DEFAULT 1 NOT NULL,
    avaliacao_pai_id integer,
    liberada boolean DEFAULT false,
    liberada_em timestamp without time zone,
    prazo_fim timestamp without time zone NOT NULL,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.avaliacoes OWNER TO postgres;

--
-- TOC entry 229 (class 1259 OID 16504)
-- Name: avaliacoes_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.avaliacoes_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.avaliacoes_id_seq OWNER TO postgres;

--
-- TOC entry 5134 (class 0 OID 0)
-- Dependencies: 229
-- Name: avaliacoes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.avaliacoes_id_seq OWNED BY public.avaliacoes.id;


--
-- TOC entry 224 (class 1259 OID 16449)
-- Name: matriculas; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.matriculas (
    id integer NOT NULL,
    turma_id integer,
    aluno_id integer,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.matriculas OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 16448)
-- Name: matriculas_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.matriculas_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.matriculas_id_seq OWNER TO postgres;

--
-- TOC entry 5135 (class 0 OID 0)
-- Dependencies: 223
-- Name: matriculas_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.matriculas_id_seq OWNED BY public.matriculas.id;


--
-- TOC entry 226 (class 1259 OID 16470)
-- Name: questoes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.questoes (
    id integer NOT NULL,
    professor_id integer,
    tipo public.tipo_questao NOT NULL,
    enunciado text NOT NULL,
    imagem_url text,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.questoes OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 16469)
-- Name: questoes_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.questoes_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.questoes_id_seq OWNER TO postgres;

--
-- TOC entry 5136 (class 0 OID 0)
-- Dependencies: 225
-- Name: questoes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.questoes_id_seq OWNED BY public.questoes.id;


--
-- TOC entry 235 (class 1259 OID 16570)
-- Name: respostas_aluno; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.respostas_aluno (
    id integer NOT NULL,
    tentativa_id integer,
    questao_id integer,
    alternativa_escolhida_id integer,
    resposta_discursiva text,
    resposta_associacao text,
    nota_atribuida numeric(5,2) DEFAULT 0.0,
    validada boolean DEFAULT false
);


ALTER TABLE public.respostas_aluno OWNER TO postgres;

--
-- TOC entry 234 (class 1259 OID 16569)
-- Name: respostas_aluno_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.respostas_aluno_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.respostas_aluno_id_seq OWNER TO postgres;

--
-- TOC entry 5137 (class 0 OID 0)
-- Dependencies: 234
-- Name: respostas_aluno_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.respostas_aluno_id_seq OWNED BY public.respostas_aluno.id;


--
-- TOC entry 233 (class 1259 OID 16548)
-- Name: tentativas_avaliacao; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tentativas_avaliacao (
    id integer NOT NULL,
    avaliacao_id integer,
    aluno_id integer,
    data_inicio timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    data_envio timestamp without time zone,
    status public.status_tentativa DEFAULT 'em_andamento'::public.status_tentativa,
    nota_total numeric(5,2) DEFAULT 0.0,
    corrigida_completamente boolean DEFAULT false
);


ALTER TABLE public.tentativas_avaliacao OWNER TO postgres;

--
-- TOC entry 232 (class 1259 OID 16547)
-- Name: tentativas_avaliacao_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tentativas_avaliacao_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tentativas_avaliacao_id_seq OWNER TO postgres;

--
-- TOC entry 5138 (class 0 OID 0)
-- Dependencies: 232
-- Name: tentativas_avaliacao_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tentativas_avaliacao_id_seq OWNED BY public.tentativas_avaliacao.id;


--
-- TOC entry 222 (class 1259 OID 16433)
-- Name: turmas; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.turmas (
    id integer NOT NULL,
    nome character varying(100) NOT NULL,
    professor_id integer,
    ativa boolean DEFAULT true,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.turmas OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 16432)
-- Name: turmas_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.turmas_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.turmas_id_seq OWNER TO postgres;

--
-- TOC entry 5139 (class 0 OID 0)
-- Dependencies: 221
-- Name: turmas_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.turmas_id_seq OWNED BY public.turmas.id;


--
-- TOC entry 220 (class 1259 OID 16416)
-- Name: usuarios; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.usuarios (
    id integer NOT NULL,
    nome character varying(100) NOT NULL,
    email character varying(150) NOT NULL,
    senha_hash character varying(255) NOT NULL,
    perfil public.perfil_usuario NOT NULL,
    criado_em timestamp(0) without time zone DEFAULT CURRENT_TIMESTAMP(0)
);


ALTER TABLE public.usuarios OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 16415)
-- Name: usuarios_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.usuarios_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.usuarios_id_seq OWNER TO postgres;

--
-- TOC entry 5140 (class 0 OID 0)
-- Dependencies: 219
-- Name: usuarios_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.usuarios_id_seq OWNED BY public.usuarios.id;


--
-- TOC entry 4913 (class 2604 OID 16491)
-- Name: alternativas id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.alternativas ALTER COLUMN id SET DEFAULT nextval('public.alternativas_id_seq'::regclass);


--
-- TOC entry 4915 (class 2604 OID 16508)
-- Name: avaliacoes id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.avaliacoes ALTER COLUMN id SET DEFAULT nextval('public.avaliacoes_id_seq'::regclass);


--
-- TOC entry 4909 (class 2604 OID 16452)
-- Name: matriculas id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.matriculas ALTER COLUMN id SET DEFAULT nextval('public.matriculas_id_seq'::regclass);


--
-- TOC entry 4911 (class 2604 OID 16473)
-- Name: questoes id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.questoes ALTER COLUMN id SET DEFAULT nextval('public.questoes_id_seq'::regclass);


--
-- TOC entry 4925 (class 2604 OID 16573)
-- Name: respostas_aluno id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.respostas_aluno ALTER COLUMN id SET DEFAULT nextval('public.respostas_aluno_id_seq'::regclass);


--
-- TOC entry 4920 (class 2604 OID 16551)
-- Name: tentativas_avaliacao id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tentativas_avaliacao ALTER COLUMN id SET DEFAULT nextval('public.tentativas_avaliacao_id_seq'::regclass);


--
-- TOC entry 4906 (class 2604 OID 16436)
-- Name: turmas id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.turmas ALTER COLUMN id SET DEFAULT nextval('public.turmas_id_seq'::regclass);


--
-- TOC entry 4904 (class 2604 OID 16419)
-- Name: usuarios id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuarios ALTER COLUMN id SET DEFAULT nextval('public.usuarios_id_seq'::regclass);


--
-- TOC entry 5120 (class 0 OID 16488)
-- Dependencies: 228
-- Data for Name: alternativas; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.alternativas (id, questao_id, texto, correta, par_associacao) FROM stdin;
\.


--
-- TOC entry 5123 (class 0 OID 16530)
-- Dependencies: 231
-- Data for Name: avaliacao_questoes; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.avaliacao_questoes (avaliacao_id, questao_id, ordem) FROM stdin;
\.


--
-- TOC entry 5122 (class 0 OID 16505)
-- Dependencies: 230
-- Data for Name: avaliacoes; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.avaliacoes (id, turma_id, titulo, peso, versao, avaliacao_pai_id, liberada, liberada_em, prazo_fim, criado_em) FROM stdin;
\.


--
-- TOC entry 5116 (class 0 OID 16449)
-- Dependencies: 224
-- Data for Name: matriculas; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.matriculas (id, turma_id, aluno_id, criado_em) FROM stdin;
\.


--
-- TOC entry 5118 (class 0 OID 16470)
-- Dependencies: 226
-- Data for Name: questoes; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.questoes (id, professor_id, tipo, enunciado, imagem_url, criado_em) FROM stdin;
\.


--
-- TOC entry 5127 (class 0 OID 16570)
-- Dependencies: 235
-- Data for Name: respostas_aluno; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.respostas_aluno (id, tentativa_id, questao_id, alternativa_escolhida_id, resposta_discursiva, resposta_associacao, nota_atribuida, validada) FROM stdin;
\.


--
-- TOC entry 5125 (class 0 OID 16548)
-- Dependencies: 233
-- Data for Name: tentativas_avaliacao; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.tentativas_avaliacao (id, avaliacao_id, aluno_id, data_inicio, data_envio, status, nota_total, corrigida_completamente) FROM stdin;
\.


--
-- TOC entry 5114 (class 0 OID 16433)
-- Dependencies: 222
-- Data for Name: turmas; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.turmas (id, nome, professor_id, ativa, criado_em) FROM stdin;
\.


--
-- TOC entry 5112 (class 0 OID 16416)
-- Dependencies: 220
-- Data for Name: usuarios; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.usuarios (id, nome, email, senha_hash, perfil, criado_em) FROM stdin;
1	Prof. Carlos	carlos@test.com	987654	professor	2026-10-24 15:24:59
2	Ana Maria	ana@aluno.test.com	testaluno	aluno	2026-10-24 15:25:55
3	Admin	admin@snctech.com	adminsntech	admin	2026-10-24 15:26:37
\.


--
-- TOC entry 5141 (class 0 OID 0)
-- Dependencies: 227
-- Name: alternativas_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.alternativas_id_seq', 1, false);


--
-- TOC entry 5142 (class 0 OID 0)
-- Dependencies: 229
-- Name: avaliacoes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.avaliacoes_id_seq', 1, false);


--
-- TOC entry 5143 (class 0 OID 0)
-- Dependencies: 223
-- Name: matriculas_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.matriculas_id_seq', 1, false);


--
-- TOC entry 5144 (class 0 OID 0)
-- Dependencies: 225
-- Name: questoes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.questoes_id_seq', 1, false);


--
-- TOC entry 5145 (class 0 OID 0)
-- Dependencies: 234
-- Name: respostas_aluno_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.respostas_aluno_id_seq', 1, false);


--
-- TOC entry 5146 (class 0 OID 0)
-- Dependencies: 232
-- Name: tentativas_avaliacao_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.tentativas_avaliacao_id_seq', 1, false);


--
-- TOC entry 5147 (class 0 OID 0)
-- Dependencies: 221
-- Name: turmas_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.turmas_id_seq', 1, false);


--
-- TOC entry 5148 (class 0 OID 0)
-- Dependencies: 219
-- Name: usuarios_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.usuarios_id_seq', 3, true);


--
-- TOC entry 4941 (class 2606 OID 16498)
-- Name: alternativas alternativas_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.alternativas
    ADD CONSTRAINT alternativas_pkey PRIMARY KEY (id);


--
-- TOC entry 4945 (class 2606 OID 16536)
-- Name: avaliacao_questoes avaliacao_questoes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.avaliacao_questoes
    ADD CONSTRAINT avaliacao_questoes_pkey PRIMARY KEY (avaliacao_id, questao_id);


--
-- TOC entry 4943 (class 2606 OID 16519)
-- Name: avaliacoes avaliacoes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.avaliacoes
    ADD CONSTRAINT avaliacoes_pkey PRIMARY KEY (id);


--
-- TOC entry 4935 (class 2606 OID 16456)
-- Name: matriculas matriculas_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.matriculas
    ADD CONSTRAINT matriculas_pkey PRIMARY KEY (id);


--
-- TOC entry 4937 (class 2606 OID 16458)
-- Name: matriculas matriculas_turma_id_aluno_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.matriculas
    ADD CONSTRAINT matriculas_turma_id_aluno_id_key UNIQUE (turma_id, aluno_id);


--
-- TOC entry 4939 (class 2606 OID 16481)
-- Name: questoes questoes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.questoes
    ADD CONSTRAINT questoes_pkey PRIMARY KEY (id);


--
-- TOC entry 4949 (class 2606 OID 16580)
-- Name: respostas_aluno respostas_aluno_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.respostas_aluno
    ADD CONSTRAINT respostas_aluno_pkey PRIMARY KEY (id);


--
-- TOC entry 4947 (class 2606 OID 16558)
-- Name: tentativas_avaliacao tentativas_avaliacao_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tentativas_avaliacao
    ADD CONSTRAINT tentativas_avaliacao_pkey PRIMARY KEY (id);


--
-- TOC entry 4933 (class 2606 OID 16442)
-- Name: turmas turmas_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.turmas
    ADD CONSTRAINT turmas_pkey PRIMARY KEY (id);


--
-- TOC entry 4929 (class 2606 OID 16431)
-- Name: usuarios usuarios_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_email_key UNIQUE (email);


--
-- TOC entry 4931 (class 2606 OID 16429)
-- Name: usuarios usuarios_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_pkey PRIMARY KEY (id);


--
-- TOC entry 4954 (class 2606 OID 16499)
-- Name: alternativas alternativas_questao_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.alternativas
    ADD CONSTRAINT alternativas_questao_id_fkey FOREIGN KEY (questao_id) REFERENCES public.questoes(id) ON DELETE CASCADE;


--
-- TOC entry 4957 (class 2606 OID 16537)
-- Name: avaliacao_questoes avaliacao_questoes_avaliacao_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.avaliacao_questoes
    ADD CONSTRAINT avaliacao_questoes_avaliacao_id_fkey FOREIGN KEY (avaliacao_id) REFERENCES public.avaliacoes(id) ON DELETE CASCADE;


--
-- TOC entry 4958 (class 2606 OID 16542)
-- Name: avaliacao_questoes avaliacao_questoes_questao_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.avaliacao_questoes
    ADD CONSTRAINT avaliacao_questoes_questao_id_fkey FOREIGN KEY (questao_id) REFERENCES public.questoes(id) ON DELETE CASCADE;


--
-- TOC entry 4955 (class 2606 OID 16525)
-- Name: avaliacoes avaliacoes_avaliacao_pai_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.avaliacoes
    ADD CONSTRAINT avaliacoes_avaliacao_pai_id_fkey FOREIGN KEY (avaliacao_pai_id) REFERENCES public.avaliacoes(id) ON DELETE SET NULL;


--
-- TOC entry 4956 (class 2606 OID 16520)
-- Name: avaliacoes avaliacoes_turma_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.avaliacoes
    ADD CONSTRAINT avaliacoes_turma_id_fkey FOREIGN KEY (turma_id) REFERENCES public.turmas(id) ON DELETE CASCADE;


--
-- TOC entry 4951 (class 2606 OID 16464)
-- Name: matriculas matriculas_aluno_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.matriculas
    ADD CONSTRAINT matriculas_aluno_id_fkey FOREIGN KEY (aluno_id) REFERENCES public.usuarios(id) ON DELETE CASCADE;


--
-- TOC entry 4952 (class 2606 OID 16459)
-- Name: matriculas matriculas_turma_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.matriculas
    ADD CONSTRAINT matriculas_turma_id_fkey FOREIGN KEY (turma_id) REFERENCES public.turmas(id) ON DELETE CASCADE;


--
-- TOC entry 4953 (class 2606 OID 16482)
-- Name: questoes questoes_professor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.questoes
    ADD CONSTRAINT questoes_professor_id_fkey FOREIGN KEY (professor_id) REFERENCES public.usuarios(id) ON DELETE CASCADE;


--
-- TOC entry 4961 (class 2606 OID 16591)
-- Name: respostas_aluno respostas_aluno_alternativa_escolhida_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.respostas_aluno
    ADD CONSTRAINT respostas_aluno_alternativa_escolhida_id_fkey FOREIGN KEY (alternativa_escolhida_id) REFERENCES public.alternativas(id);


--
-- TOC entry 4962 (class 2606 OID 16586)
-- Name: respostas_aluno respostas_aluno_questao_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.respostas_aluno
    ADD CONSTRAINT respostas_aluno_questao_id_fkey FOREIGN KEY (questao_id) REFERENCES public.questoes(id) ON DELETE CASCADE;


--
-- TOC entry 4963 (class 2606 OID 16581)
-- Name: respostas_aluno respostas_aluno_tentativa_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.respostas_aluno
    ADD CONSTRAINT respostas_aluno_tentativa_id_fkey FOREIGN KEY (tentativa_id) REFERENCES public.tentativas_avaliacao(id) ON DELETE CASCADE;


--
-- TOC entry 4959 (class 2606 OID 16564)
-- Name: tentativas_avaliacao tentativas_avaliacao_aluno_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tentativas_avaliacao
    ADD CONSTRAINT tentativas_avaliacao_aluno_id_fkey FOREIGN KEY (aluno_id) REFERENCES public.usuarios(id) ON DELETE CASCADE;


--
-- TOC entry 4960 (class 2606 OID 16559)
-- Name: tentativas_avaliacao tentativas_avaliacao_avaliacao_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tentativas_avaliacao
    ADD CONSTRAINT tentativas_avaliacao_avaliacao_id_fkey FOREIGN KEY (avaliacao_id) REFERENCES public.avaliacoes(id) ON DELETE CASCADE;


--
-- TOC entry 4950 (class 2606 OID 16443)
-- Name: turmas turmas_professor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.turmas
    ADD CONSTRAINT turmas_professor_id_fkey FOREIGN KEY (professor_id) REFERENCES public.usuarios(id) ON DELETE CASCADE;


-- Completed on 2026-10-24 16:25:04

--
-- PostgreSQL database dump complete
--

\unrestrict OV1DgoeL8dVbPgFm5X8mXCIgHwg6F0t01JIr6LC4vKBWaCClrqbTzRkYQ1T7Xgx

