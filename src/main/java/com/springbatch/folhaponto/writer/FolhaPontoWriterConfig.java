package com.springbatch.folhaponto.writer;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import com.springbatch.folhaponto.dominio.FolhaPonto;

@Configuration
public class FolhaPontoWriterConfig {

	@StepScope
	@Bean
	public FlatFileItemWriter<FolhaPonto> folhaPontoWriter(
			@Value("#{jobParameters['folhaPonto']}") Resource folhaPonto) {
		return new FlatFileItemWriterBuilder<FolhaPonto>()
				.name("folhaPontoWriter")
				.resource(folhaPonto)
				.lineAggregator(lineAggregator())
				.headerCallback(cabecalhoCallback())
				.footerCallback(rodapeCallback())
				.build();
	}
	
	@StepScope
	@Bean
	public FlatFileItemWriter<FolhaPonto> funcionarioSemPontoWriter(
			@Value("#{jobParameters['funcionarioSemPonto']}") Resource folhaPonto) {
		return new FlatFileItemWriterBuilder<FolhaPonto>()
				.name("funcionarioSemPontoWriter")
				.resource(folhaPonto)
				.lineAggregator(lineAggregatorSemPonto())
				.build();
	}

	private LineAggregator<FolhaPonto> lineAggregatorSemPonto() {
		return new LineAggregator<FolhaPonto>() {
			
			@Override
			public String aggregate(FolhaPonto folhaPonto) {	
				StringBuilder writer = new StringBuilder();
				writer.append(imprimeFuncionariosSemPontos(folhaPonto));						
				return writer.toString();
			}
		};
	}

	protected String imprimeFuncionariosSemPontos(FolhaPonto folhaPonto) {
		StringBuilder writer = new StringBuilder();
		if(folhaPonto.getRegistrosPontos().isEmpty()) {
			writer.append(folhaPonto.getMatricula());
		}
		return writer.toString();
	}

	private FlatFileFooterCallback rodapeCallback() {
		return new FlatFileFooterCallback() {
			
			@Override
			public void writeFooter(Writer writer) throws IOException {
				writer.append(String.format("\n\t\t\t\t\t\t\t  Código de Autenticação: %s\n", "fkyew6868fewjfhjjewf"));				
			}
		};
	}

	private FlatFileHeaderCallback cabecalhoCallback() {
		return new FlatFileHeaderCallback() {
			
			@Override
			public void writeHeader(Writer writer) throws IOException {
				writer.append(String.format("SISTEMA INTEGRADO: XPTO \t\t\t\t DATA: %s\n",
						new SimpleDateFormat("dd/MM/yyyy").format(new Date())));
				writer.append(String.format("MÓDULO: RH \t\t\t\t\t\t\t\t HORA: %s\n",
						new SimpleDateFormat("HH:MM").format(new Date())));
				writer.append(String.format("\t\t\t\tFOLHA DE PONTO\n"));
				
			}
		};
	}

	private LineAggregator<FolhaPonto> lineAggregator() {
		return new LineAggregator<FolhaPonto>() {

			@Override
			public String aggregate(FolhaPonto folhaPonto) {	
				StringBuilder writer = new StringBuilder();
				writer.append(imprimePontos(folhaPonto));						
				return writer.toString();
			}
		};
	}

	private String imprimePontos(FolhaPonto folhaPonto) {
		StringBuilder writer = new StringBuilder();
		
		if(!folhaPonto.getRegistrosPontos().isEmpty()) {
			writer.append(String.format("----------------------------------------------------------------------------\n"));
			writer.append(String.format("NOME:%s\n", folhaPonto.getNome()));
			writer.append(String.format("MATRICULA:%s\n", folhaPonto.getMatricula()));
			writer.append(String.format("----------------------------------------------------------------------------\n"));
			writer.append(String.format("%10s%10s%10s%10s%10s", "DATA", "ENTRADA", "SAIDA", "ENTRADA", "SAIDA"));

			for (String dataRegistroPonto : folhaPonto.getRegistrosPontos().keySet()) {
				writer.append(String.format("\n%s", dataRegistroPonto));

				for (String registro : folhaPonto.getRegistrosPontos().get(dataRegistroPonto)) {
					writer.append(String.format("%10s", registro));
				}
			}	
		}	

		return writer.toString();
	}

}
