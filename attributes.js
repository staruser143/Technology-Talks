import { Injectable } from '@nestjs/common';
import { CommandRouter } from './command-router.service';

@Injectable()
export class CommandDispatcher {
  constructor(private readonly router: CommandRouter) {}

  async dispatch(requestDto: any) {
    // Extract request attributes (including nested ones)
    const requestAttributes = {
      requestType: requestDto.requestType,
      requestSource: requestDto.requestSource,
      userRole: requestDto.user.role,          // Extracted from nested "user" object
      userRegion: requestDto.user.region,
      policyType: requestDto.policyDetails.coverageType
    };

    // Find the matching handlers based on extracted attributes
    const handlerNames = this.router.findMatchingHandlers(requestAttributes);

    if (handlerNames.length === 0) {
      throw new Error('No matching handler found');
    }

    console.log(`Executing handlers: ${handlerNames.join(', ')}`);

    // Execute all matching handlers
    for (const handlerName of handlerNames) {
      const handler = this.router.getHandlerInstance(handlerName);
      await handler.handle(requestDto);
    }

    return { status: 'success', executedHandlers: handlerNames };
  }
}