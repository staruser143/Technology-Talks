import { Injectable } from '@nestjs/common';
import * as fs from 'fs';
import * as yaml from 'yaml';

interface HandlerConfig {
  name: string;
  priority: number;
  conditions: Record<string, any>;
}

@Injectable()
export class CommandRouter {
  private handlerConfigs: HandlerConfig[];

  constructor() {
    this.handlerConfigs = this.loadConfig();
  }

  private loadConfig(): HandlerConfig[] {
    const file = fs.readFileSync('src/config/config.yml', 'utf8');
    return yaml.parse(file).handlers;
  }

  findMatchingHandler(requestAttributes: Record<string, any>): string | null {
    return this.handlerConfigs
      .filter(config => this.matches(config, requestAttributes))
      .sort((a, b) => b.priority - a.priority) // Highest priority first
      .map(config => config.name)[0] || null;
  }

  private matches(config: HandlerConfig, requestAttributes: Record<string, any>): boolean {
    return Object.entries(config.conditions).every(([key, value]) => {
      if (Array.isArray(value)) {
        return value.includes(requestAttributes[key]);
      }
      return requestAttributes[key] === value;
    });
  }
}